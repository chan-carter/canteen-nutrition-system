package com.health.canteen.mapper;

import com.health.canteen.entity.Recommendation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface RecommendationMapper {

    /**
     * 清空某学生的历史推荐记录
     */
    void deleteByUserId(Integer userId);

    /**
     * 批量保存推荐记录
     */
    void batchAdd(List<Recommendation> recommendations);

    /**
     * 查询某学生的推荐记录
     */
    List<Recommendation> getByUserId(Integer userId);

    /**
     * 统计菜品被推荐次数
     */
    List<Map<String, Object>> getRecommendHotData();

}
