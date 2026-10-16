package com.health.canteen.service.impl;

import com.health.canteen.ai.AiChatClient;
import com.health.canteen.dto.AiChatDTO;
import com.health.canteen.entity.AiChatMessage;
import com.health.canteen.entity.HealthProfile;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.AiChatMapper;
import com.health.canteen.mapper.HealthProfileMapper;
import com.health.canteen.service.AiAssistantService;
import com.health.canteen.service.HealthService;
import com.health.canteen.utils.CurrentUser;
import com.health.canteen.vo.AiChatReplyVO;
import com.health.canteen.vo.DailyIntakeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI 营养问答助手：把学生档案与今日摄入作为上下文，带最近对话历史做多轮问答；
 * 未配置大模型密钥时降级为基于目标的规则回答，功能不中断。
 */
@Slf4j
@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final int HISTORY_LIMIT = 10;

    @Autowired
    private AiChatMapper aiChatMapper;

    @Autowired
    private HealthProfileMapper healthProfileMapper;

    @Autowired
    private HealthService healthService;

    @Autowired
    private AiChatClient aiChatClient;

    @Override
    public AiChatReplyVO chat(AiChatDTO dto) {
        if (dto.getMessage() == null || dto.getMessage().trim().isEmpty()) {
            throw new BusinessException("请输入你的问题");
        }
        Integer userId = CurrentUser.id();
        String sessionId = (dto.getSessionId() == null || dto.getSessionId().trim().isEmpty())
                ? UUID.randomUUID().toString().replace("-", "") : dto.getSessionId().trim();
        String question = dto.getMessage().trim();

        List<AiChatMessage> history = aiChatMapper.listBySession(userId, sessionId, HISTORY_LIMIT);
        Collections.reverse(history); //查询按新到旧，喂给模型要按旧到新

        List<Map<String, String>> messages = AiChatClient.newMessages();
        messages.add(AiChatClient.msg("system", buildSystemPrompt()));
        for (AiChatMessage h : history) {
            messages.add(AiChatClient.msg(h.getRole(), h.getContent()));
        }
        messages.add(AiChatClient.msg("user", question));

        persist(userId, sessionId, "user", question, null, null, null);

        AiChatReplyVO vo = new AiChatReplyVO();
        vo.setSessionId(sessionId);
        if (aiChatClient.available()) {
            AiChatClient.ChatResult r = aiChatClient.chat(messages);
            if (r.isSuccess() && r.getContent() != null) {
                vo.setReply(r.getContent().trim());
                vo.setModelName(r.getModelName());
                vo.setCostMs(r.getCostMs());
                vo.setSuccess(true);
                vo.setSource("AI模型");
                persist(userId, sessionId, "assistant", vo.getReply(), r.getModelName(), 1, r.getCostMs());
                return vo;
            }
            log.warn("AI问答降级, 原因: {}", r.getErrorMsg());
        }
        String fallback = ruleBasedReply();
        vo.setReply(fallback);
        vo.setModelName("rule-based-v1");
        vo.setSuccess(true);
        vo.setSource("内置规则");
        persist(userId, sessionId, "assistant", fallback, "rule-based-v1", 1, 0);
        return vo;
    }

    @Override
    public List<AiChatMessage> history(String sessionId, int limit) {
        int n = limit <= 0 ? 50 : Math.min(limit, 200);
        List<AiChatMessage> list = aiChatMapper.listBySession(CurrentUser.id(), sessionId, n);
        Collections.reverse(list);
        return list;
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("你是校园食堂的营养助手，面向学生解答菜品营养、减脂增肌饮食搭配、今天吃得健不健康等问题。")
                .append("请用简洁的中文回答，控制在150字以内，条理清楚，不要输出Markdown。")
                .append("所有营养数值均为估算，仅供参考，不能替代医生或营养师的专业意见。\n")
                .append("【学生当前情况】\n").append(contextLine());
        return sb.toString();
    }

    private String contextLine() {
        HealthProfile profile = healthProfileMapper.getByUserId(CurrentUser.id());
        DailyIntakeVO today = healthService.dailyIntake(null);
        StringBuilder c = new StringBuilder();
        if (profile != null) {
            c.append("健康目标：").append(profile.getHealthGoal())
                    .append("；每日目标热量约").append(profile.getTargetCalorie()).append("kcal，")
                    .append("蛋白").append(profile.getTargetProtein()).append("g、脂肪")
                    .append(profile.getTargetFat()).append("g、碳水").append(profile.getTargetCarb()).append("g。\n");
        } else {
            c.append("尚未建立身体档案（无个性化目标）。\n");
        }
        c.append("今日已摄入约：热量").append(today.getCalorie()).append("kcal、蛋白")
                .append(today.getProtein()).append("g、脂肪").append(today.getFat()).append("g、碳水")
                .append(today.getCarbohydrate()).append("g。");
        return c.toString();
    }

    private String ruleBasedReply() {
        HealthProfile profile = healthProfileMapper.getByUserId(CurrentUser.id());
        DailyIntakeVO today = healthService.dailyIntake(null);
        StringBuilder sb = new StringBuilder();
        sb.append("（未接入大模型，以下为系统按目标自动估算的提示）\n");
        sb.append("今日已摄入约 ").append(today.getCalorie()).append(" kcal");
        if (profile != null && profile.getTargetCalorie() != null) {
            double remaining = profile.getTargetCalorie().doubleValue() - today.getCalorie().doubleValue();
            sb.append("，目标 ").append(profile.getTargetCalorie()).append(" kcal（").append(profile.getHealthGoal()).append("）");
            if (remaining >= 0) {
                sb.append("，还可摄入约 ").append(String.format("%.0f", remaining)).append(" kcal。");
            } else {
                sb.append("，已超出约 ").append(String.format("%.0f", -remaining)).append(" kcal，建议清淡为主。");
            }
        } else {
            sb.append("。建议先到「健康档案」完善身高体重与目标，获得个性化建议。");
        }
        sb.append("（数值为估算，仅供参考）");
        return sb.toString();
    }

    private void persist(Integer userId, String sessionId, String role, String content,
                         String model, Integer success, Integer costMs) {
        AiChatMessage m = new AiChatMessage();
        m.setUserId(userId);
        m.setSessionId(sessionId);
        m.setRole(role);
        m.setContent(content);
        m.setModelName(model);
        m.setSuccess(success);
        m.setCostMs(costMs);
        m.setCreateTime(LocalDateTime.now());
        aiChatMapper.insert(m);
    }
}
