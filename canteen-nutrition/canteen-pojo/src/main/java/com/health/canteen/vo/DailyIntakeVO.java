package com.health.canteen.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 某一天摄入与目标的对比（用于进度环）
 */
@Data
public class DailyIntakeVO {
    private LocalDate date;
    private boolean hasProfile; //是否已建立身体档案（否则无目标）
    private BigDecimal targetCalorie;
    private BigDecimal targetProtein;
    private BigDecimal targetFat;
    private BigDecimal targetCarb;
    private BigDecimal calorie;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbohydrate;
    private BigDecimal sodium;
    private Integer recordCount;
}
