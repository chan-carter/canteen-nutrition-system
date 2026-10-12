package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单查询条件
 */
@Data
@NoArgsConstructor
public class OrderPageQueryDTO extends PageQueryDTO {
    private Integer status;
    private String orderType;
    private Integer userId;
    private Integer merchantId; //服务端注入，商户只能查看本商户订单
}
