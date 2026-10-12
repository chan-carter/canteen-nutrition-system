package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户列表查询条件
 */
@Data
@NoArgsConstructor
public class UserPageQueryDTO extends PageQueryDTO {
    private String username;
    private Integer role;
    private Integer status;
}
