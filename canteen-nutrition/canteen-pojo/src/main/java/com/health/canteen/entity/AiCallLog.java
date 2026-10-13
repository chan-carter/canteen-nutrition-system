package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI接口调用日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiCallLog {
    private Integer id;
    private Integer dishId;
    private String callType; //调用类型：NUTRITION/RECOMMEND
    private String modelName;
    private Integer success; //1成功，0失败
    private Long costMs;
    private String errorMsg;
    private String rawContent;
    private LocalDateTime createTime;
}
