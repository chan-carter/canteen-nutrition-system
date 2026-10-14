package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 个性化推荐记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    private Integer id;
    private Integer userId;
    private Integer dishId;
    private String reason; //推荐理由
    private BigDecimal score; //推荐得分
    private String healthGoal; //生成依据的健康目标
    private LocalDateTime createTime;

    private String dishName;
    private String nickname;
}
