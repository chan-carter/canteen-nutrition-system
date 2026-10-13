package com.health.canteen.ai;

import com.health.canteen.vo.NutritionAnalysisVO;
import java.util.List;

/**
 * 营养分析器：屏蔽具体AI实现，便于替换模型或降级
 */
public interface NutritionAnalyzer {

    /**
     * 根据菜品名称与主要食材估算营养数据
     *
     * @param dishName     菜品名称
     * @param ingredients  主要食材列表
     * @param servingSize  参考份量，可为空
     * @return 分析结果，调用失败时 success 为 false 并携带失败原因
     */
    NutritionAnalysisVO analyze(String dishName, List<String> ingredients, String servingSize);

}
