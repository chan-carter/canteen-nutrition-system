package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 下单请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSubmitDTO {
    private List<OrderItemDTO> items;
    private String orderType; //PICKUP点餐，RESERVE预订
    private String remark;
}
