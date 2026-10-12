package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 食堂商户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {
    private Integer id; //ID
    private String name; //商户名称
    private String contact; //联系人
    private String phone; //联系电话
    private String address; //地址
    private String description; //商户介绍
    private Integer status; //状态：1正常，0停业
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer stallCount; //所属档口数量
}
