package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐查询条件
 */
@Data
@NoArgsConstructor
public class RecommendationPageQueryDTO extends PageQueryDTO {
    private String healthGoal;
    private Integer categoryId;
    private Integer maxCalories;
}
