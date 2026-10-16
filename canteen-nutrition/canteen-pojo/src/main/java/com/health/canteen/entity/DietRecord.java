package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日饮食摄入记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietRecord {
    private Integer id;
    private Integer userId;
    private Integer dishId;
    private String dishName;
    private LocalDate recordDate;
    private String mealType; //早餐/午餐/晚餐/加餐
    private BigDecimal calorie;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbohydrate;
    private BigDecimal sodium;
    private String source; //order/manual
    private Integer orderItemId;
    private LocalDateTime createTime;
}
