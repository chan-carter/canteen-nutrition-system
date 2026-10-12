package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 档口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stall {
    private Integer id; //ID
    private Integer merchantId; //所属商户ID
    private String name; //档口名称
    private String location; //档口位置
    private String businessHours; //营业时间
    private String description; //档口介绍
    private Integer status; //状态：1营业，0关闭
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String merchantName; //所属商户名称
    private Integer dishCount; //档口菜品数量
}
