package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜品分类查询条件
 */
@Data
@NoArgsConstructor
public class CategoryPageQueryDTO extends PageQueryDTO {
    private String name;
    private Integer status;
}
