package com.health.canteen.mapper;

import com.health.canteen.entity.RecommendFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecommendFeedbackMapper {

    /**
     * 一人对一菜只保留最新一条反馈（依赖 uk_fb_user_dish）
     */
    void upsert(RecommendFeedback feedback);

    List<RecommendFeedback> listByUser(@Param("userId") Integer userId);
}
