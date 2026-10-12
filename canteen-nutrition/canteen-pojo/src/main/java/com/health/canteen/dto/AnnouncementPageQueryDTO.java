package com.health.canteen.dto;

import lombok.Data;

/**
 * 公告分页查询条件
 */
@Data
public class AnnouncementPageQueryDTO extends PageQueryDTO {

    private String title;

    private Integer status;

}
