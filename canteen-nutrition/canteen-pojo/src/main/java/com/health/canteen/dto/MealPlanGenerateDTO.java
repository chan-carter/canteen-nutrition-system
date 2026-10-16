package com.health.canteen.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 生成周期餐计划
 */
@Data
public class MealPlanGenerateDTO {
    private LocalDate startDate;
    private Integer days; //默认7天
}
