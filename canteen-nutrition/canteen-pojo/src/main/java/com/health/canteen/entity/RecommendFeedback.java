package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 个性化推荐反馈（赞/踩），用于动态调权
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendFeedback {
    private Integer id;
    private Integer userId;
    private Integer dishId;
    private Integer action; //1赞，-1踩
    private String reason;
    private LocalDateTime createTime;
}
