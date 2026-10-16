package com.health.canteen.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 记录一次体重
 */
@Data
public class WeightLogDTO {
    private BigDecimal weightKg;
    private LocalDate recordDate;
    private String note;
}
