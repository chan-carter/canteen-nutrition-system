package com.health.canteen.service;

import com.health.canteen.dto.OrderPageQueryDTO;
import com.health.canteen.dto.OrderSubmitDTO;
import com.health.canteen.entity.Orders;
import com.health.canteen.result.PageResult;

import java.util.List;

public interface OrderService {

    Orders createOrder(OrderSubmitDTO orderSubmitDto);

    PageResult<Orders> getOrdersPage(OrderPageQueryDTO orderQueryParam);

    Orders getOrdersById(Integer id);

    void updateStatus(Integer id, Integer status);

    void deleteOrders(List<Integer> ids);

    /**
     * 取消超过指定分钟数仍未接单的订单，返回取消笔数（定时任务调用）
     */
    int cancelTimeoutOrders(int minutes);

}
