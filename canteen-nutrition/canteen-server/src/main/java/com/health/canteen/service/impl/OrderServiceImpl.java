package com.health.canteen.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.health.canteen.constant.RedisConstant;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.DishMapper;
import com.health.canteen.mapper.OrderItemMapper;
import com.health.canteen.mapper.OrdersMapper;
import com.health.canteen.context.*;
import com.health.canteen.dto.*;
import com.health.canteen.entity.*;
import com.health.canteen.result.*;
import com.health.canteen.vo.*;
import com.health.canteen.service.OrderService;
import com.health.canteen.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private static final int STATUS_WAIT = 1;
    private static final int STATUS_TAKEN = 2;
    private static final int STATUS_DONE = 3;
    private static final int STATUS_CANCELLED = 4;
    private static final int STATUS_PICKUP = 5; //待取餐
    private static final int STATUS_MAX = 5;

    /**
     * 订单状态机：待接单→已接单/已取消，已接单→待取餐/已取消，待取餐→已完成/已取消，
     * 已完成与已取消是终态。不加这层校验时，已完成的订单可以被改回待接单，而销量只在完成时累加，会被重复计数。
     */
    private static final Map<Integer, List<Integer>> NEXT_STATUS = Map.of(
            STATUS_WAIT, List.of(STATUS_TAKEN, STATUS_CANCELLED),
            STATUS_TAKEN, List.of(STATUS_PICKUP, STATUS_CANCELLED),
            STATUS_PICKUP, List.of(STATUS_DONE, STATUS_CANCELLED),
            STATUS_DONE, List.of(),
            STATUS_CANCELLED, List.of());

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private com.health.canteen.service.NotificationService notificationService;

    @Override
    @Transactional
    @CacheEvict(cacheNames = RedisConstant.DISH_CACHE, allEntries = true)
    public Orders createOrder(OrderSubmitDTO orderSubmitDto) {
        if (orderSubmitDto == null || orderSubmitDto.getItems() == null || orderSubmitDto.getItems().isEmpty()) {
            throw new BusinessException("请至少选择一个菜品");
        }

        Integer merchantId = null;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalCalorie = null;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemDTO itemDto : orderSubmitDto.getItems()) {
            if (itemDto.getDishId() == null || itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                throw new BusinessException("菜品数量必须大于0");
            }
            Dish dish = dishMapper.getDishById(itemDto.getDishId());
            if (dish == null) {
                throw new BusinessException("菜品不存在, ID: " + itemDto.getDishId());
            }
            if (dish.getStatus() == null || dish.getStatus() != 1) {
                throw new BusinessException("菜品【" + dish.getName() + "】已下架，无法下单");
            }
            if (!Integer.valueOf(1).equals(dish.getAuditStatus())) {
                throw new BusinessException("菜品【" + dish.getName() + "】尚未通过审核，无法下单");
            }
            // 每日限量按当天未取消的订单累计，库存只在下单时扣减，取消时归还
            if (dish.getDailyLimit() != null) {
                int sold = dishMapper.countTodaySold(dish.getId());
                if (sold + itemDto.getQuantity() > dish.getDailyLimit()) {
                    throw new BusinessException("菜品【" + dish.getName() + "】今日限量" + dish.getDailyLimit()
                            + "份，仅剩" + Math.max(0, dish.getDailyLimit() - sold) + "份");
                }
            }
            if (dish.getStock() != null && dishMapper.minusStock(dish.getId(), itemDto.getQuantity()) == 0) {
                throw new BusinessException("菜品【" + dish.getName() + "】库存不足，仅剩" + dish.getStock() + "份");
            }
            if (merchantId == null) {
                merchantId = dish.getMerchantId();
            } else if (!merchantId.equals(dish.getMerchantId())) {
                throw new BusinessException("同一订单的菜品需要属于同一个食堂商户");
            }

            BigDecimal amount = dish.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(amount);

            OrderItem item = new OrderItem();
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setQuantity(itemDto.getQuantity());
            item.setPrice(dish.getPrice());
            item.setAmount(amount);
            items.add(item);

            if (dish.getCalorie() != null) {
                totalCalorie = (totalCalorie == null ? BigDecimal.ZERO : totalCalorie)
                        .add(dish.getCalorie().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            }
        }

        Orders orders = new Orders();
        orders.setOrderNo(generateOrderNo());
        orders.setUserId(CurrentUser.id());
        orders.setMerchantId(merchantId);
        orders.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        orders.setTotalCalorie(totalCalorie == null ? null : totalCalorie.setScale(1, RoundingMode.HALF_UP));
        orders.setOrderType(resolveOrderType(orderSubmitDto.getOrderType()));
        orders.setStatus(STATUS_WAIT);
        orders.setRemark(orderSubmitDto.getRemark());
        orders.setCreateTime(LocalDateTime.now());
        orders.setUpdateTime(LocalDateTime.now());
        ordersMapper.addOrder(orders);

        for (OrderItem item : items) {
            item.setOrderId(orders.getId());
        }
        orderItemMapper.batchAdd(items);
        log.info("订单创建成功, 订单号: {}, 金额: {}, 明细数: {}", orders.getOrderNo(), orders.getTotalAmount(), items.size());

        orders.setItems(items);
        return orders;
    }

    @Override
    public PageResult<Orders> getOrdersPage(OrderPageQueryDTO orderQueryParam) {
        if (CurrentUser.isStudent()) {
            orderQueryParam.setUserId(CurrentUser.id());
            orderQueryParam.setMerchantId(null);
        } else if (CurrentUser.isMerchant()) {
            orderQueryParam.setMerchantId(CurrentUser.merchantId());
            orderQueryParam.setUserId(null);
        }
        PageHelper.startPage(orderQueryParam.getPage(), orderQueryParam.getPageSize());
        List<Orders> orders = ordersMapper.getOrdersPage(orderQueryParam);
        Page<Orders> page = (Page<Orders>) orders;
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    public Orders getOrdersById(Integer id) {
        Orders orders = ordersMapper.getOrdersById(id);
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        checkOrderVisible(orders);
        orders.setItems(orderItemMapper.getByOrderId(id));
        return orders;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = RedisConstant.DISH_CACHE, allEntries = true)
    public void updateStatus(Integer id, Integer status) {
        if (status == null || status < STATUS_WAIT || status > STATUS_MAX) {
            throw new BusinessException("订单状态取值不正确");
        }
        Orders orders = ordersMapper.getOrdersById(id);
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        if (CurrentUser.isMerchant()) {
            CurrentUser.checkMerchantOwner(orders.getMerchantId());
        } else if (CurrentUser.isStudent()) {
            //学生只能取消自己的待接单订单
            if (!orders.getUserId().equals(CurrentUser.id())) {
                throw new BusinessException("只能操作本人的订单");
            }
            if (status != STATUS_CANCELLED) {
                throw new BusinessException("学生只能取消订单");
            }
        }
        int from = orders.getStatus() == null ? STATUS_WAIT : orders.getStatus();
        if (from == status) {
            throw new BusinessException("订单已经是该状态");
        }
        if (!NEXT_STATUS.get(from).contains(status)) {
            throw new BusinessException("订单状态不允许从「" + statusName(from) + "」变更为「" + statusName(status) + "」");
        }

        LocalDateTime now = LocalDateTime.now();
        String pickupCode = null;
        LocalDateTime acceptTime = null;
        LocalDateTime finishTime = null;
        if (status == STATUS_TAKEN) {
            pickupCode = generatePickupCode();
            acceptTime = now;
        } else if (status == STATUS_DONE) {
            finishTime = now;
        }
        ordersMapper.updateStatusFlow(id, status, pickupCode, acceptTime, finishTime, now);

        //销量只在订单完成的那一刻累加一次，避免取消单计入销量，也避免状态回退后重复累加
        if (status == STATUS_DONE) {
            for (OrderItem item : orderItemMapper.getByOrderId(id)) {
                dishMapper.increaseSales(item.getDishId(), item.getQuantity());
            }
            log.info("订单完成，按明细累加销量, 订单号: {}", orders.getOrderNo());
        }
        if (status == STATUS_CANCELLED) {
            restoreStock(id);
        }

        notificationService.push(orders.getUserId(), "order", "订单" + statusName(status),
                buildOrderNotice(orders.getOrderNo(), status, pickupCode), id);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = RedisConstant.DISH_CACHE, allEntries = true)
    public int cancelTimeoutOrders(int minutes) {
        List<Orders> timeoutOrders = ordersMapper.listTimeoutWait(LocalDateTime.now().minusMinutes(Math.max(1, minutes)));
        if (timeoutOrders.isEmpty()) {
            return 0;
        }
        List<Integer> ids = new ArrayList<>();
        for (Orders order : timeoutOrders) {
            ids.add(order.getId());
        }
        ordersMapper.cancelByIds(ids, LocalDateTime.now());
        for (Orders order : timeoutOrders) {
            restoreStock(order.getId());
            notificationService.push(order.getUserId(), "order", "订单" + statusName(STATUS_CANCELLED),
                    buildOrderNotice(order.getOrderNo(), STATUS_CANCELLED, null), order.getId());
        }
        log.info("超时未接单订单自动取消 {} 笔", ids.size());
        return ids.size();
    }

    private void restoreStock(Integer orderId) {
        for (OrderItem item : orderItemMapper.getByOrderId(orderId)) {
            dishMapper.plusStock(item.getDishId(), item.getQuantity());
        }
    }

    private String buildOrderNotice(String orderNo, int status, String pickupCode) {
        switch (status) {
            case STATUS_TAKEN:
                return "订单 " + orderNo + " 已接单，取餐码 " + pickupCode;
            case STATUS_PICKUP:
                return "订单 " + orderNo + " 已出餐，请凭取餐码尽快取餐";
            case STATUS_DONE:
                return "订单 " + orderNo + " 已完成，欢迎再次光临";
            case STATUS_CANCELLED:
                return "订单 " + orderNo + " 已取消";
            default:
                return "订单 " + orderNo + " 状态已更新";
        }
    }

    private String generatePickupCode() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private String statusName(int status) {
        switch (status) {
            case STATUS_TAKEN:
                return "已接单";
            case STATUS_PICKUP:
                return "待取餐";
            case STATUS_DONE:
                return "已完成";
            case STATUS_CANCELLED:
                return "已取消";
            default:
                return "待接单";
        }
    }

    @Override
    @Transactional
    public void deleteOrders(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的订单");
        }
        orderItemMapper.deleteByOrderIds(ids);
        ordersMapper.deleteOrders(ids);
    }

    private void checkOrderVisible(Orders orders) {
        if (CurrentUser.isAdmin()) {
            return;
        }
        if (CurrentUser.isStudent()) {
            if (!orders.getUserId().equals(CurrentUser.id())) {
                throw new BusinessException("只能查看本人的订单");
            }
            return;
        }
        CurrentUser.checkMerchantOwner(orders.getMerchantId());
    }

    private String resolveOrderType(String orderType) {
        if (orderType == null || orderType.trim().isEmpty()) {
            return "PICKUP";
        }
        String type = orderType.trim().toUpperCase();
        if (!"PICKUP".equals(type) && !"RESERVE".equals(type)) {
            throw new BusinessException("订单类型只能是PICKUP或RESERVE");
        }
        return type;
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return date + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

}
