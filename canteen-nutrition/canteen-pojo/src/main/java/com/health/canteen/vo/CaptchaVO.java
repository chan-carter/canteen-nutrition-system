package com.health.canteen.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图形验证码：key 为服务端存 Redis 的凭据，提交注册时原样带回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVO {

    private String key;

    /**
     * data:image/png;base64 形式，前端可直接作为 img 的 src
     */
    private String image;

}
