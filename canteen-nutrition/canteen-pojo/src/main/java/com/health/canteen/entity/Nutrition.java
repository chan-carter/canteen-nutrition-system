package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品营养分析结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nutrition {
    private Integer id; //ID
    private Integer dishId; //菜品ID
    private BigDecimal calorie; //热量 kcal
    private BigDecimal protein; //蛋白质 g
    private BigDecimal fat; //脂肪 g
    private BigDecimal carbohydrate; //碳水化合物 g
    private BigDecimal fiber; //膳食纤维 g
    private BigDecimal sodium; //钠 mg
    private String analysisText; //分析说明
    private String source; //数据来源说明
    private String modelName; //模型或规则标识
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String dishName; //菜品名称
    private String ingredients; //主要食材
}
