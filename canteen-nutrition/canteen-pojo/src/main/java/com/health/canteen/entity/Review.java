package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 菜品评价
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Integer id;
    private Integer userId;
    private Integer dishId;
    private Integer orderId;
    private Integer rating; //评分1-5
    private String content; //评价内容
    private Integer status; //1正常，0已屏蔽
    private LocalDateTime createTime;

    private String nickname; //学生昵称
    private String dishName; //菜品名称
}
