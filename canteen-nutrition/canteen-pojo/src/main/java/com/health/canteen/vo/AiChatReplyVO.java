package com.health.canteen.vo;

import lombok.Data;

/**
 * 一次问答的返回
 */
@Data
public class AiChatReplyVO {
    private String sessionId;
    private String reply;
    private String modelName;
    private boolean success;
    private Integer costMs;
    private String source; //AI模型 / 内置规则
}
