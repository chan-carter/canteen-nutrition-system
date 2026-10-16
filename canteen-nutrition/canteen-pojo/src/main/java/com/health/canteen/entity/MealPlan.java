package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 周期餐计划条目：某天某餐安排的一道菜
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealPlan {
    private Integer id;
    private Integer userId;
    private LocalDate planDate;
    private String mealType; //早餐/午餐/晚餐/加餐
    private Integer dishId;
    private String dishName;
    private BigDecimal plannedCalorie;
    private Integer adopted; //1已下单采用，0未采用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
