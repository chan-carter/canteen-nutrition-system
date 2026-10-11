package com.health.canteen.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI大模型接口配置（OpenAI兼容协议）
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * 接口地址，例如 https://dashscope.aliyuncs.com/compatible-mode/v1
     */
    private String baseUrl;

    /**
     * 密钥，为空时营养分析直接走内置规则估算
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 单次调用超时时间（秒）
     */
    private Integer timeoutSeconds = 20;

}
