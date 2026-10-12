package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评价查询条件
 */
@Data
@NoArgsConstructor
public class ReviewPageQueryDTO extends PageQueryDTO {
    private Integer dishId;
    private Integer userId;
    private Integer rating;
    private Integer status;
}
