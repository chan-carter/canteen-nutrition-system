package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 营养问答对话消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessage {
    private Integer id;
    private Integer userId;
    private String sessionId;
    private String role; //user/assistant
    private String content;
    private String modelName;
    private Integer success; //1成功，0失败
    private Integer costMs;
    private LocalDateTime createTime;
}
