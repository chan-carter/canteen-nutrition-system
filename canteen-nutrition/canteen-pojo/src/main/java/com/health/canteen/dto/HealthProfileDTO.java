package com.health.canteen.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 身体档案录入：BMR/TDEE/目标由服务端根据这些输入计算，前端不传
 */
@Data
public class HealthProfileDTO {
    private Integer gender; //1男，2女
    private LocalDate birthday;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private Integer activityLevel; //1久坐，2轻度，3中度，4重度
    private String healthGoal; //减脂/增肌/均衡
}
