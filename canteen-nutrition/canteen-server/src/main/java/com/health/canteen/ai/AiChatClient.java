package com.health.canteen.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.canteen.properties.AiProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容协议的多轮对话客户端，供 AI 营养问答助手使用。
 * 与营养分析器共用 base-url / model / api-key 配置。
 */
@Slf4j
@Component
public class AiChatClient {

    @Autowired
    private AiProperties aiProperties;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public boolean available() {
        return aiProperties.getApiKey() != null && !aiProperties.getApiKey().trim().isEmpty();
    }

    public String model() {
        return aiProperties.getModel();
    }

    /**
     * @param messages 已组装好的对话消息（含 system 与历史）
     */
    public ChatResult chat(List<Map<String, String>> messages) {
        ChatResult result = new ChatResult();
        result.setModelName(aiProperties.getModel());
        if (!available()) {
            result.setSuccess(false);
            result.setErrorMsg("未配置AI接口密钥");
            return result;
        }
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", aiProperties.getModel());
            body.put("messages", messages);
            body.put("temperature", 0.4);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(trimUrl(aiProperties.getBaseUrl()) + "/chat/completions"))
                    .timeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds() == null ? 20 : aiProperties.getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            result.setCostMs((int) (System.currentTimeMillis() - start));
            if (response.statusCode() != 200) {
                result.setSuccess(false);
                result.setErrorMsg("AI接口返回状态码 " + response.statusCode());
                log.warn("AI问答调用异常, 状态码: {}", response.statusCode());
                return result;
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                result.setSuccess(false);
                result.setErrorMsg("AI返回内容为空");
                return result;
            }
            result.setContent(content.asText());
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            result.setCostMs((int) (System.currentTimeMillis() - start));
            result.setSuccess(false);
            result.setErrorMsg(e.getClass().getSimpleName() + " - " + e.getMessage());
            log.error("AI问答调用失败", e);
            return result;
        }
    }

    private String trimUrl(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    @Data
    public static class ChatResult {
        private String content;
        private String modelName;
        private boolean success;
        private Integer costMs;
        private String errorMsg;
    }

    /**
     * 便捷构造一条消息
     */
    public static Map<String, String> msg(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    public static List<Map<String, String>> newMessages() {
        return new ArrayList<>();
    }
}
