package com.health.canteen.controller.common;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.dto.NutritionAnalyzeDTO;
import com.health.canteen.result.Result;
import com.health.canteen.service.NutritionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("commonNutritionController")
public class NutritionController {

    @Autowired
    private NutritionService nutritionService;

    /**
     * 查询菜品营养信息
     */
    @GetMapping("/dishes/{id}/nutrition")
    public Result getNutrition(@PathVariable Integer id) {
        log.info("查询菜品营养信息, 菜品ID: {}", id);
        return Result.success(nutritionService.getByDishId(id));
    }

}
