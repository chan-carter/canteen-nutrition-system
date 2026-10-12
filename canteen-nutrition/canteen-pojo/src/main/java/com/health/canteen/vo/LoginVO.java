package com.health.canteen.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功返回的数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private Integer id;
    private String username;
    private String nickname;
    private Integer role;
    private Integer merchantId;
    private String token;
}
