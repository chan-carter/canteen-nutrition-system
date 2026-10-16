package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {
    private Long id;
    private Integer userId;
    private String username;
    private String module;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private Integer costMs;
    private Integer success;
    private String errorMsg;
    private LocalDateTime createTime;
}
