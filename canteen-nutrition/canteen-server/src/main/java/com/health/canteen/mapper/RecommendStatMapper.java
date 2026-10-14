package com.health.canteen.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 协同过滤所需的订单共现统计（只读）
 */
@Mapper
public interface RecommendStatMapper {

    /**
     * 该学生历史点过的菜品ID
     */
    List<Integer> listUserOrderedDishIds(@Param("userId") Integer userId);

    /**
     * 与该学生点过的菜同单出现的其它菜品及共现次数
     */
    List<Map<String, Object>> listCooccurringDishes(@Param("userId") Integer userId, @Param("limit") int limit);
}
