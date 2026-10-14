package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Orders {
    private Integer id; //ID
    private String orderNo; //订单编号
    private Integer userId; //下单学生ID
    private Integer merchantId; //所属商户ID
    private BigDecimal totalAmount; //订单总金额
    private BigDecimal totalCalorie; //估算总热量
    private String orderType; //PICKUP点餐，RESERVE预订
    private Integer status; //1待接单，2已接单/制作中，5待取餐，3已完成，4已取消
    private String pickupCode; //取餐码
    private LocalDateTime payTime; //支付时间
    private LocalDateTime acceptTime; //接单时间
    private LocalDateTime finishTime; //完成时间
    private String remark; //备注
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String userNickname; //下单学生昵称
    private String merchantName; //商户名称
    private List<OrderItem> items; //订单明细
}
