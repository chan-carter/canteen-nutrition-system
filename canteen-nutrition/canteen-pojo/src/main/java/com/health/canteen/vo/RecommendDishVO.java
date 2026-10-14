package com.health.canteen.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 推荐结果条目
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendDishVO {
    private Integer dishId; //菜品ID
    private String dishName; //菜品名称
    private BigDecimal price; //价格
    private String image; //图片
    private String ingredients; //主要食材
    private Integer stallId;
    private String stallName; //档口名称
    private Integer categoryId;
    private String categoryName; //分类名称
    private BigDecimal calorie; //热量
    private BigDecimal protein; //蛋白质
    private BigDecimal fat; //脂肪
    private BigDecimal carbohydrate; //碳水化合物
    private BigDecimal fiber; //膳食纤维
    private BigDecimal sodium; //钠
    private BigDecimal avgRating; //平均评分
    private String reason; //推荐理由
    private BigDecimal score; //推荐得分
}
