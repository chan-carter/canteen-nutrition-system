package com.health.canteen.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置：访问凭证不放在配置文件中，由 SDK 直接读取环境变量
 * OSS_ACCESS_KEY_ID / OSS_ACCESS_KEY_SECRET
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOSSProperties {

    /**
     * 访问域名，如 https://oss-cn-beijing.aliyuncs.com
     */
    private String endpoint;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 所在地域，如 cn-beijing，V4签名必填
     */
    private String region;

}
