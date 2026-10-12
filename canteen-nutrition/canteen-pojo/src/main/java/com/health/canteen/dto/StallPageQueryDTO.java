package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 档口列表查询条件
 */
@Data
@NoArgsConstructor
public class StallPageQueryDTO extends PageQueryDTO {
    private Integer merchantId;
    private String name;
    private Integer status;
}
