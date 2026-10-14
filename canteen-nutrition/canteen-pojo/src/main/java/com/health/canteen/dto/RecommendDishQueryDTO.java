package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐菜品候选项查询条件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendDishQueryDTO {
    private Integer categoryId; //限定分类
    private Integer maxCalories; //单份最高热量
    private Integer merchantId; //限定商户
    private Integer limitSize; //最多取多少条候选
}
