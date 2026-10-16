package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生菜品收藏
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {
    private Integer id;
    private Integer userId;
    private Integer dishId;
    private LocalDateTime createTime;
}
