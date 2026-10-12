package com.health.canteen.dto;

import lombok.Data;

/**
 * 分页查询基类：在取值时归一化页码与每页数量，避免请求缺少分页参数时报错或一次拉取过多数据
 */
@Data
public class PageQueryDTO {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private Integer page;

    private Integer pageSize;

    public Integer getPage() {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

}
