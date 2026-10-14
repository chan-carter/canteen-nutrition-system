package com.health.canteen.service;

import com.health.canteen.result.PageResult;
import com.health.canteen.vo.RecommendDishVO;
import com.health.canteen.dto.RecommendationPageQueryDTO;

public interface RecommendationService {

    /**
     * 根据健康目标、饮食偏好和营养数据生成个性化推荐
     */
    PageResult<RecommendDishVO> getRecommendations(RecommendationPageQueryDTO queryParam);

    /**
     * 按当前登录学生的资料重新生成并保存推荐记录
     */
    int refresh();

    /**
     * 对某道推荐菜品点赞/踩，用于后续推荐动态调权
     */
    void feedback(Integer dishId, Integer action, String reason);

}
