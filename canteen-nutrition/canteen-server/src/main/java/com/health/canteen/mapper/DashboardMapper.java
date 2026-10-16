package com.health.canteen.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 数据大屏聚合统计（只读）
 */
@Mapper
public interface DashboardMapper {

    Map<String, Object> globalStats();

    List<Map<String, Object>> categoryDist();

    List<Map<String, Object>> topDishes();

    List<Map<String, Object>> calorieDist();

    Map<String, Object> aiStats();

    Map<String, Object> healthStats();

    List<Map<String, Object>> orderStatusDist();
}
