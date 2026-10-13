package com.health.canteen.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 营养分析结果
 */
@Data
public class NutritionAnalysisVO {

    private BigDecimal calorie; //热量 kcal
    private BigDecimal protein; //蛋白质 g
    private BigDecimal fat; //脂肪 g
    private BigDecimal carbohydrate; //碳水化合物 g
    private BigDecimal fiber; //膳食纤维 g
    private BigDecimal sodium; //钠 mg
    private String analysisText; //分析说明
    private String source; //数据来源说明
    private String modelName; //模型或规则标识
    private Long costMs; //耗时
    private boolean success; //是否调用成功
    private String errorMsg; //失败原因
    private String rawContent; //模型原始返回

}
