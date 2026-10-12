package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生自助注册参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String healthGoal;
    private String preference;
    private String taboo;
    private String captchaKey; //图形验证码凭据，由 /captcha 下发
    private String captchaCode;
}
