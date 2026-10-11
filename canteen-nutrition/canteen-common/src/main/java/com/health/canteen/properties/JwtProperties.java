package com.health.canteen.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * 令牌签名密钥
     */
    private String secret;

    /**
     * 令牌有效期（毫秒）
     */
    private Long ttl;

}
