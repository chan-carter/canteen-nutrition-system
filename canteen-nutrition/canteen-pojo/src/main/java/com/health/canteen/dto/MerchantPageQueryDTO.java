package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商户列表查询条件
 */
@Data
@NoArgsConstructor
public class MerchantPageQueryDTO extends PageQueryDTO {
    private String name;
    private Integer status;
}
