package com.health.canteen.dto;

import lombok.Data;

/**
 * 对某条推荐点赞/踩
 */
@Data
public class RecommendFeedbackDTO {
    private Integer dishId;
    private Integer action; //1赞，-1踩
    private String reason;
}
