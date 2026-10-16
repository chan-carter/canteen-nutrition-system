package com.health.canteen.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 手动记录一笔饮食摄入
 */
@Data
public class DietRecordDTO {
    private Integer dishId; //可空，手动录入自由文本时为空
    private String dishName;
    private LocalDate recordDate;
    private String mealType; //早餐/午餐/晚餐/加餐
    private BigDecimal calorie;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbohydrate;
    private BigDecimal sodium;
}
