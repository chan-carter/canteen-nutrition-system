package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateDTO {
    private String oldPassword;
    private String newPassword;
}
