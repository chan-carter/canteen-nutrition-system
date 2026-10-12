package com.health.canteen.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id; //ID
    private String username; //登录账号
    // 只允许写入（管理员新增用户时接收明文密码），任何响应都不返回密码
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password; //密码（BCrypt密文，不返回给前端）
    private String nickname; //昵称
    private Integer role; //角色：1学生，2食堂商户，3管理员
    private String phone; //手机号
    private Integer merchantId; //所属商户ID
    private String healthGoal; //健康目标
    private String preference; //饮食偏好
    private String taboo; //忌口信息
    private Integer status; //状态：1正常，0禁用
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //修改时间

    private String merchantName; //所属商户名称
}
