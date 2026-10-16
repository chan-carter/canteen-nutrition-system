package com.health.canteen.controller.student;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.dto.AiChatDTO;
import com.health.canteen.result.Result;
import com.health.canteen.service.AiAssistantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生端 AI 营养问答助手（仅学生）
 */
@Slf4j
@RequestMapping("/ai")
@RestController("studentAiAssistantController")
@RequireRole(1)
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public Result chat(@RequestBody AiChatDTO dto) {
        log.info("AI营养问答");
        return Result.success(aiAssistantService.chat(dto));
    }

    @GetMapping("/chat/history")
    public Result history(@RequestParam String sessionId,
                          @RequestParam(defaultValue = "50") int limit) {
        return Result.success(aiAssistantService.history(sessionId, limit));
    }

}
