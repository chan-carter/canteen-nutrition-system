package com.health.canteen.dto;

import lombok.Data;

/**
 * 发起一次营养问答
 */
@Data
public class AiChatDTO {
    private String sessionId; //为空则服务端新建会话
    private String message;
}
