package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 站内消息通知
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Integer id;
    private Integer userId;
    private String type; //order/audit/announcement/system
    private String title;
    private String content;
    private Integer bizId;
    private Integer isRead;
    private LocalDateTime createTime;
}
