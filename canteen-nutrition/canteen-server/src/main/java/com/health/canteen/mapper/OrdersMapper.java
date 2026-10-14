package com.health.canteen.mapper;

import com.health.canteen.dto.OrderPageQueryDTO;
import com.health.canteen.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrdersMapper {

    /**
     * 新增订单
     */
    void addOrder(Orders orders);

    /**
     * 条件分页查询订单
     */
    List<Orders> getOrdersPage(OrderPageQueryDTO orderQueryParam);

    /**
     * 查询订单详情
     */
    Orders getOrdersById(Integer id);

    /**
     * 修改订单状态
     */
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 状态流转并写入取餐码/接单时间/完成时间（仅非空字段更新）
     */
    void updateStatusFlow(@Param("id") Integer id, @Param("status") Integer status,
                          @Param("pickupCode") String pickupCode, @Param("acceptTime") LocalDateTime acceptTime,
                          @Param("finishTime") LocalDateTime finishTime, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 查询超过指定时刻仍处于待接单的订单
     */
    List<Orders> listTimeoutWait(@Param("before") LocalDateTime before);

    /**
     * 批量取消订单
     */
    void cancelByIds(@Param("ids") List<Integer> ids, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 批量删除订单
     */
    void deleteOrders(List<Integer> ids);

}
