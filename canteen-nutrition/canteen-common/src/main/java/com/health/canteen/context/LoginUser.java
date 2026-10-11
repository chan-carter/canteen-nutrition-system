package com.health.canteen.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录用户信息，由JWT令牌解析得到，保存在ThreadLocal中供业务层使用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    private Integer userId;
    private String username;
    private Integer role; //角色：1学生，2食堂商户，3管理员
    private Integer merchantId; //商户账号所属商户ID，用于数据隔离
}
