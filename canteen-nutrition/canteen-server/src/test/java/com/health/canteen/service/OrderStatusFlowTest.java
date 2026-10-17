package com.health.canteen.service;

import com.health.canteen.context.BaseContext;
import com.health.canteen.context.LoginUser;
import com.health.canteen.entity.OrderItem;
import com.health.canteen.entity.Orders;
import com.health.canteen.exception.AccessDeniedException;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单状态机与越权防线。销量只在「完成」那一刻累加，所以一旦允许 3→1 这类回退，
 * 同一笔订单就能被反复计数；这组用例钉住的是允许的流转集合与角色边界。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单状态流转")
class OrderStatusFlowTest {

    private static final int WAIT = 1;
    private static final int TAKEN = 2;
    private static final int DONE = 3;
    private static final int CANCELLED = 4;
    private static final int PICKUP = 5;

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
    void loginAsMerchant() {
        BaseContext.set(new LoginUser(4, "canteen01", 2, 1));
    }

    @AfterEach
    void logout() {
        BaseContext.remove();
    }

    private void givenOrder(Integer id, Integer status, Integer userId, Integer merchantId) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setOrderNo("202610160001");
        orders.setStatus(status);
        orders.setUserId(userId);
        orders.setMerchantId(merchantId);
        orders.setCreateTime(LocalDateTime.now());
        when(ordersMapper.getOrdersById(id)).thenReturn(orders);
    }

    @Test
    @DisplayName("待接单→已接单：生成4位取餐码并记接单时间")
    void acceptGeneratesPickupCode() {
        givenOrder(1, WAIT, 1, 1);

        orderService.updateStatus(1, TAKEN);

        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(ordersMapper).updateStatusFlow(eq(1), eq(TAKEN), code.capture(), any(LocalDateTime.class),
                isNull(), any(LocalDateTime.class));
        assertThat(code.getValue()).matches("\\d{4}");
        verify(notificationService).push(eq(1), eq("order"), eq("订单已接单"), any(String.class), eq(1));
    }

    @Test
    @DisplayName("已接单→待取餐→已完成：完成时才累加销量")
    void pickupThenDone() {
        givenOrder(1, TAKEN, 1, 1);
        orderService.updateStatus(1, PICKUP);
        verify(ordersMapper).updateStatusFlow(eq(1), eq(PICKUP), isNull(), isNull(), isNull(), any(LocalDateTime.class));
        verify(dishMapper, never()).increaseSales(anyInt(), anyInt());

        givenOrder(1, PICKUP, 1, 1);
        when(orderItemMapper.getByOrderId(1)).thenReturn(List.of(item(101, 2), item(105, 1)));
        orderService.updateStatus(1, DONE);

        verify(dishMapper).increaseSales(101, 2);
        verify(dishMapper).increaseSales(105, 1);
        verify(ordersMapper).updateStatusFlow(eq(1), eq(DONE), isNull(), isNull(), any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    @DisplayName("终态不可回退，非法流转全部拒绝")
    void rejectsIllegalTransitions() {
        givenOrder(1, DONE, 1, 1);
        assertThatThrownBy(() -> orderService.updateStatus(1, WAIT))
                .isInstanceOf(BusinessException.class).hasMessageContaining("不允许");
        assertThatThrownBy(() -> orderService.updateStatus(1, TAKEN))
                .isInstanceOf(BusinessException.class).hasMessageContaining("已完成");

        givenOrder(2, CANCELLED, 1, 1);
        assertThatThrownBy(() -> orderService.updateStatus(2, WAIT))
                .isInstanceOf(BusinessException.class).hasMessageContaining("已取消");

        givenOrder(3, WAIT, 1, 1);
        assertThatThrownBy(() -> orderService.updateStatus(3, WAIT))
                .isInstanceOf(BusinessException.class).hasMessageContaining("已经是该状态");

        assertThatThrownBy(() -> orderService.updateStatus(3, 9))
                .isInstanceOf(BusinessException.class).hasMessageContaining("状态取值");
        assertThatThrownBy(() -> orderService.updateStatus(3, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("状态取值");

        verify(ordersMapper, never()).updateStatusFlow(anyInt(), anyInt(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("跳过中间态不允许：待接单不能直接完成")
    void cannotSkipPickup() {
        givenOrder(1, WAIT, 1, 1);

        assertThatThrownBy(() -> orderService.updateStatus(1, DONE))
                .isInstanceOf(BusinessException.class).hasMessageContaining("不允许");
    }

    @Test
    @DisplayName("商户只能处理本商户订单")
    void merchantScopedToOwnMerchant() {
        givenOrder(1, WAIT, 1, 2);

        assertThatThrownBy(() -> orderService.updateStatus(1, TAKEN)).isInstanceOf(AccessDeniedException.class);
        verify(ordersMapper, never()).updateStatusFlow(anyInt(), anyInt(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("学生只能取消本人订单，不能推进状态")
    void studentCanOnlyCancelOwn() {
        BaseContext.set(new LoginUser(1, "zhangsan", 1, null));
        givenOrder(1, WAIT, 2, 1);
        assertThatThrownBy(() -> orderService.updateStatus(1, CANCELLED))
                .isInstanceOf(BusinessException.class).hasMessageContaining("本人");

        givenOrder(2, WAIT, 1, 1);
        assertThatThrownBy(() -> orderService.updateStatus(2, TAKEN))
                .isInstanceOf(BusinessException.class).hasMessageContaining("只能取消");

        givenOrder(3, WAIT, 1, 1);
        when(orderItemMapper.getByOrderId(3)).thenReturn(List.of(item(101, 1)));
        orderService.updateStatus(3, CANCELLED);
        verify(dishMapper).plusStock(101, 1);
    }

    @Test
    @DisplayName("取消订单归还库存，超时批量取消同样归还并通知")
    void cancelRestoresStock() {
        givenOrder(1, WAIT, 1, 1);
        when(orderItemMapper.getByOrderId(1)).thenReturn(List.of(item(101, 3)));
        orderService.updateStatus(1, CANCELLED);
        verify(dishMapper).plusStock(101, 3);

        Orders timeout = new Orders();
        timeout.setId(7);
        timeout.setOrderNo("202610160007");
        timeout.setUserId(1);
        timeout.setStatus(WAIT);
        when(ordersMapper.listTimeoutWait(any(LocalDateTime.class))).thenReturn(List.of(timeout));
        when(orderItemMapper.getByOrderId(7)).thenReturn(List.of(item(105, 2)));

        int cancelled = orderService.cancelTimeoutOrders(30);

        assertThat(cancelled).isEqualTo(1);
        verify(ordersMapper).cancelByIds(eq(List.of(7)), any(LocalDateTime.class));
        verify(dishMapper).plusStock(105, 2);
        verify(notificationService).push(eq(1), eq("order"), eq("订单已取消"), any(String.class), eq(7));
    }

    @Test
    @DisplayName("没有超时订单时不动库不通知")
    void nothingToCancel() {
        when(ordersMapper.listTimeoutWait(any(LocalDateTime.class))).thenReturn(List.of());

        assertThat(orderService.cancelTimeoutOrders(30)).isZero();
        verify(ordersMapper, never()).cancelByIds(any(), any());
        verify(notificationService, never()).push(any(), any(), any(), any(), any());
    }

    private OrderItem item(Integer dishId, Integer quantity) {
        OrderItem item = new OrderItem();
        item.setDishId(dishId);
        item.setQuantity(quantity);
        return item;
    }

}
