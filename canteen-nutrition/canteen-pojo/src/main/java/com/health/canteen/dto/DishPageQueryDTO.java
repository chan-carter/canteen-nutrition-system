package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜品列表查询条件
 */
@Data
@NoArgsConstructor
public class DishPageQueryDTO extends PageQueryDTO {
    private String name;
    private Integer stallId;
    private Integer categoryId;
    private Integer status;
    private Integer auditStatus;
    private Integer merchantId; //按商户过滤（服务端注入，商户只能查询自身菜品）
}
