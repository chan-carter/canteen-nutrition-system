package com.health.canteen.service;

import com.health.canteen.dto.AiChatDTO;
import com.health.canteen.entity.AiChatMessage;
import com.health.canteen.vo.AiChatReplyVO;

import java.util.List;

public interface AiAssistantService {

    AiChatReplyVO chat(AiChatDTO dto);

    List<AiChatMessage> history(String sessionId, int limit);
}
