package com.health.canteen.ai;

import com.health.canteen.vo.NutritionAnalysisVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.canteen.properties.AiProperties;
import com.health.canteen.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * OpenAI兼容协议的大模型营养分析器
 * 通过 base-url / model / api-key 配置切换供应商，无需修改代码
 */
@Slf4j
@Component
public class OpenAiNutritionAnalyzer implements NutritionAnalyzer {

    private static final String SYSTEM_PROMPT =
            "你是一名熟悉中式菜肴的大学食堂营养师。请根据用户给出的菜品名称和主要食材，估算一份（默认约250克）该菜品的营养数据。"
                    + "要求：只输出一个JSON对象，不要输出Markdown代码块、解释文字或其他内容。"
                    + "JSON字段为：calorie(热量,kcal,数字)、protein(蛋白质,g,数字)、fat(脂肪,g,数字)、"
                    + "carbohydrate(碳水化合物,g,数字)、fiber(膳食纤维,g,数字)、sodium(钠,mg,数字)、"
                    + "analysisText(不超过120字的中文营养说明，需指出主要供能来源和食用建议)。"
                    + "所有数值保留一位小数，无法确定的字段给出合理估算值。";

    @Autowired
    private AiProperties aiProperties;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public NutritionAnalysisVO analyze(String dishName, List<String> ingredients, String servingSize) {
        NutritionAnalysisVO result = new NutritionAnalysisVO();
        result.setModelName(aiProperties.getModel());

        if (isBlank(aiProperties.getApiKey())) {
            result.setSuccess(false);
            result.setErrorMsg("未配置AI接口密钥(ai.api-key)");
            return result;
        }

        long start = System.currentTimeMillis();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(trimUrl(aiProperties.getBaseUrl()) + "/chat/completions"))
                    .timeout(Duration.ofSeconds(timeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(buildBody(dishName, ingredients, servingSize), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            result.setCostMs(System.currentTimeMillis() - start);

            if (response.statusCode() != 200) {
                log.warn("AI接口调用异常, 状态码: {}, 返回: {}", response.statusCode(), abbreviate(response.body()));
                result.setSuccess(false);
                result.setErrorMsg("AI接口返回状态码 " + response.statusCode());
                return result;
            }

            String content = extractContent(response.body());
            result.setRawContent(content);
            if (isBlank(content)) {
                result.setSuccess(false);
                result.setErrorMsg("AI接口返回内容为空");
                return result;
            }

            fillFromJson(result, content);
            if (result.getCalorie() == null || result.getProtein() == null || result.getFat() == null) {
                result.setSuccess(false);
                result.setErrorMsg("AI返回的数据格式不符合约定，缺少热量/蛋白质/脂肪字段");
                return result;
            }
            if (result.getAnalysisText() == null) {
                result.setAnalysisText("根据菜品名称和主要食材估算生成");
            }
            result.setSource("AI模型估算，仅供参考");
            result.setSuccess(true);
            log.info("AI营养分析成功, 菜品: {}, 模型: {}, 耗时: {}ms", dishName, aiProperties.getModel(), result.getCostMs());
            return result;
        } catch (Exception e) {
            result.setCostMs(System.currentTimeMillis() - start);
            result.setSuccess(false);
            result.setErrorMsg("调用AI接口失败：" + e.getClass().getSimpleName() + " - " + abbreviate(e.getMessage()));
            log.error("AI营养分析失败, 菜品: {}", dishName, e);
            return result;
        }
    }

    private String buildBody(String dishName, List<String> ingredients, String servingSize) throws Exception {
        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);

        StringBuilder userContent = new StringBuilder();
        userContent.append("菜品名称：").append(dishName).append("\n");
        userContent.append("主要食材：").append(String.join("、", ingredients == null ? new ArrayList<>() : ingredients)).append("\n");
        userContent.append("参考份量：").append(isBlank(servingSize) ? "约一份（250克）" : servingSize).append("\n");
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userContent.toString());

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(systemMsg);
        messages.add(userMsg);

        Map<String, Object> body = new HashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("messages", messages);
        body.put("temperature", 0.2);
        return objectMapper.writeValueAsString(body);
    }

    private String extractContent(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return null;
        }
        JsonNode content = choices.get(0).path("message").path("content");
        return content.isMissingNode() || content.isNull() ? null : content.asText();
    }

    private void fillFromJson(NutritionAnalysisVO result, String content) throws Exception {
        JsonNode node = objectMapper.readTree(JsonUtils.extractJsonObject(content));
        result.setCalorie(toDecimal(node, "calorie"));
        result.setProtein(toDecimal(node, "protein"));
        result.setFat(toDecimal(node, "fat"));
        result.setCarbohydrate(toDecimal(node, "carbohydrate"));
        result.setFiber(toDecimal(node, "fiber"));
        result.setSodium(toDecimal(node, "sodium"));
        JsonNode text = node.path("analysisText");
        result.setAnalysisText(text.isMissingNode() || text.isNull() ? null : text.asText());
    }

    private BigDecimal toDecimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        try {
            BigDecimal decimal = new BigDecimal(value.asText().trim());
            return decimal.setScale(1, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private int timeoutSeconds() {
        return aiProperties.getTimeoutSeconds() == null ? 20 : aiProperties.getTimeoutSeconds();
    }

    private String trimUrl(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 300 ? text : text.substring(0, 300) + "...";
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

}
