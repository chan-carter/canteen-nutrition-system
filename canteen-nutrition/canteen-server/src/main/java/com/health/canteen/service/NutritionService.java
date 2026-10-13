package com.health.canteen.service;

import com.health.canteen.entity.Nutrition;
import com.health.canteen.dto.NutritionAnalyzeDTO;

public interface NutritionService {

    /**
     * 调用AI大模型分析菜品营养并保存结果
     */
    Nutrition analyze(Integer dishId, NutritionAnalyzeDTO analyzeParam);

    /**
     * 查询菜品的营养分析结果
     */
    Nutrition getByDishId(Integer dishId);

}
