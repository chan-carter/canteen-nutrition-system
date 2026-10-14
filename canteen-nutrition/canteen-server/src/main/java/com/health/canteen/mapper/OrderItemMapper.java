package com.health.canteen.mapper;

import com.health.canteen.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    /**
     * 批量新增订单明细
     */
    void batchAdd(List<OrderItem> items);

    /**
     * 查询订单明细
     */
    List<OrderItem> getByOrderId(Integer orderId);

    /**
     * 根据订单ID批量删除明细
     */
    void deleteByOrderIds(List<Integer> orderIds);

}
