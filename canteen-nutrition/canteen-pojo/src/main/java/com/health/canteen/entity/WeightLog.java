package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体重趋势记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeightLog {
    private Integer id;
    private Integer userId;
    private BigDecimal weightKg;
    private LocalDate recordDate;
    private String note;
    private LocalDateTime createTime;
}
