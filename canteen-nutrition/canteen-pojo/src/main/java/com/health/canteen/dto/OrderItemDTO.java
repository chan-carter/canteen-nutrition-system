package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单菜品明细
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Integer dishId;
    private Integer quantity;
}
