package com.health.canteen.controller.merchant;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.dto.NutritionAnalyzeDTO;
import com.health.canteen.result.Result;
import com.health.canteen.service.NutritionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("merchantNutritionController")
public class NutritionController {

    @Autowired
    private NutritionService nutritionService;

    /**
     * 调用AI大模型分析菜品营养
     */
    @PostMapping("/dishes/{id}/nutrition/analyze")
    @RequireRole({2, 3})
    public Result analyze(@PathVariable Integer id, @RequestBody(required = false) NutritionAnalyzeDTO analyzeParam) {
        log.info("发起菜品营养分析, 菜品ID: {}", id);
        return Result.success(nutritionService.analyze(id, analyzeParam));
    }

}
