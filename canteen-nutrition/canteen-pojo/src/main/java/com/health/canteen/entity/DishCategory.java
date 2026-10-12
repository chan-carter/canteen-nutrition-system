package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 菜品分类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishCategory {
    private Integer id; //ID
    private String name; //分类名称
    private Integer sortNum; //排序号
    private String remark; //备注
    private Integer status; //状态：1启用，0停用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer dishCount; //该分类下菜品数量
}
