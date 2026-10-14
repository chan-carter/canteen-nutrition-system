package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单明细
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Integer id;
    private Integer orderId;
    private Integer dishId;
    private String dishName; //下单时菜品名称快照
    private Integer quantity;
    private BigDecimal price; //下单时单价快照
    private BigDecimal amount; //小计

    private String image; //菜品图片
}
