package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统公告
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Announcement {
    private Integer id;
    private String title;
    private String content;
    private Integer status; //1发布，0下线
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
