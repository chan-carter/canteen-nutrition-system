package com.health.canteen.service;

import com.health.canteen.context.BaseContext;
import com.health.canteen.context.LoginUser;
import com.health.canteen.dto.OrderItemDTO;
import com.health.canteen.dto.OrderSubmitDTO;
import com.health.canteen.entity.Dish;
import com.health.canteen.entity.Orders;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.DishMapper;
import com.health.canteen.mapper.OrderItemMapper;
import com.health.canteen.mapper.OrdersMapper;
import com.health.canteen.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 下单防线：库存与每日限量是商户在后台真配的字段，扣减靠带条件的 UPDATE 完成
 * （stock >= 数量 才更新成功），所以并发下不会扣成负数；这里校验的就是这些拒绝分支。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("下单与库存校验")
class OrderCreateStockTest {

    @Mock
    private OrdersMapper ordersMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void loginAsStudent() {
        BaseContext.set(new LoginUser(1, "zhangsan", 1, null));
    }

    @AfterEach
    void logout() {
        BaseContext.remove();
    }

    private Dish dish(int id, String price, String calorie) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName("菜品" + id);
        dish.setPrice(new BigDecimal(price));
        dish.setCalorie(new BigDecimal(calorie));
        dish.setStatus(1);
        dish.setAuditStatus(1);
        dish.setMerchantId(1);
        return dish;
    }

    private OrderSubmitDTO order(Integer... dishIdAndQty) {
        List<OrderItemDTO> items = new java.util.ArrayList<>();
        for (int i = 0; i < dishIdAndQty.length; i += 2) {
            items.add(new OrderItemDTO(dishIdAndQty[i], dishIdAndQty[i + 1]));
        }
        OrderSubmitDTO dto = new OrderSubmitDTO();
        dto.setItems(items);
        dto.setOrderType("PICKUP");
        return dto;
    }

    @Test
    @DisplayName("正常下单：金额与热量按明细汇总，状态为待接单")
    void createsOrder() {
        when(dishMapper.getDishById(101)).thenReturn(dish(101, "8.5", "185"));
        when(dishMapper.getDishById(105)).thenReturn(dish(105, "22", "260"));

        orderService.createOrder(order(101, 2, 105, 1));

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersMapper).addOrder(captor.capture());
        Orders saved = captor.getValue();
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("39.00");
        assertThat(saved.getTotalCalorie()).isEqualByComparingTo("630.0");
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(saved.getUserId()).isEqualTo(1);
        assertThat(saved.getMerchantId()).isEqualTo(1);
        assertThat(saved.getOrderNo()).hasSize(12);
        verify(orderItemMapper).batchAdd(any());
    }

    @Test
    @DisplayName("库存不足时拒绝下单，且整单不落库")
    void rejectsWhenStockShort() {
        Dish limited = dish(101, "8.5", "185");
        limited.setStock(1);
        when(dishMapper.getDishById(101)).thenReturn(limited);
        when(dishMapper.minusStock(101, 2)).thenReturn(0);

        assertThatThrownBy(() -> orderService.createOrder(order(101, 2)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("库存不足");

        verify(ordersMapper, never()).addOrder(any());
        verify(orderItemMapper, never()).batchAdd(any());
    }

    @Test
    @DisplayName("超出每日限量时给出剩余份数")
    void rejectsWhenDailyLimitReached() {
        Dish limited = dish(101, "8.5", "185");
        limited.setDailyLimit(5);
        when(dishMapper.getDishById(101)).thenReturn(limited);
        when(dishMapper.countTodaySold(101)).thenReturn(4);

        assertThatThrownBy(() -> orderService.createOrder(order(101, 2)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("限量").hasMessageContaining("仅剩1份");
        verify(ordersMapper, never()).addOrder(any());
    }

    @Test
    @DisplayName("库存充足的分支会真正扣减")
    void deductsStockWhenEnough() {
        Dish enough = dish(101, "8.5", "185");
        enough.setStock(10);
        when(dishMapper.getDishById(101)).thenReturn(enough);
        when(dishMapper.minusStock(101, 2)).thenReturn(1);

        orderService.createOrder(order(101, 2));

        verify(dishMapper).minusStock(101, 2);
        verify(ordersMapper).addOrder(any());
    }

    @Test
    @DisplayName("下架、未过审、不存在的菜品都不能下单")
    void rejectsUnavailableDishes() {
        Dish off = dish(101, "8.5", "185");
        off.setStatus(0);
        when(dishMapper.getDishById(101)).thenReturn(off);
        assertThatThrownBy(() -> orderService.createOrder(order(101, 1)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("已下架");

        Dish unaudited = dish(102, "9", "200");
        unaudited.setAuditStatus(0);
        when(dishMapper.getDishById(102)).thenReturn(unaudited);
        assertThatThrownBy(() -> orderService.createOrder(order(102, 1)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("审核");

        when(dishMapper.getDishById(999)).thenReturn(null);
        assertThatThrownBy(() -> orderService.createOrder(order(999, 1)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("同一订单的菜品必须属于同一商户，数量必须为正")
    void validatesCartShape() {
        Dish mine = dish(101, "8.5", "185");
        Dish others = dish(102, "9", "200");
        others.setMerchantId(2);
        when(dishMapper.getDishById(101)).thenReturn(mine);
        when(dishMapper.getDishById(102)).thenReturn(others);

        assertThatThrownBy(() -> orderService.createOrder(order(101, 1, 102, 1)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("同一个食堂商户");

        assertThatThrownBy(() -> orderService.createOrder(order(101, 0)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("大于0");

        assertThatThrownBy(() -> orderService.createOrder(new OrderSubmitDTO()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("至少选择一个");
    }

    @Test
    @DisplayName("用餐方式只接受 PICKUP / RESERVE")
    void validatesOrderType() {
        when(dishMapper.getDishById(101)).thenReturn(dish(101, "8.5", "185"));
        OrderSubmitDTO dto = order(101, 1);
        dto.setOrderType("DINE_IN");

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(BusinessException.class).hasMessageContaining("PICKUP");
    }

}
