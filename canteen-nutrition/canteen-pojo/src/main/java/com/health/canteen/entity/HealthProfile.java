package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生身体档案与每日营养目标
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfile {
    private Integer id;
    private Integer userId;
    private Integer gender; //1男，2女
    private LocalDate birthday;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private Integer activityLevel; //1久坐，2轻度，3中度，4重度
    private String healthGoal; //减脂/增肌/均衡
    private BigDecimal bmr; //基础代谢率
    private BigDecimal tdee; //每日总消耗
    private BigDecimal targetCalorie;
    private BigDecimal targetProtein;
    private BigDecimal targetFat;
    private BigDecimal targetCarb;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
