package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜品审核参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishAuditDTO {
    private Integer auditStatus; //1通过，2驳回
    private String auditRemark; //审核意见
}
