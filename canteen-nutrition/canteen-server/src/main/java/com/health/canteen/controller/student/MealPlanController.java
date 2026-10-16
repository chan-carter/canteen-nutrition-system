package com.health.canteen.controller.student;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.dto.MealPlanGenerateDTO;
import com.health.canteen.result.Result;
import com.health.canteen.service.MealPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 学生端周期餐计划（仅学生）
 */
@Slf4j
@RequestMapping("/meal-plans")
@RestController("studentMealPlanController")
@RequireRole(1)
public class MealPlanController {

    @Autowired
    private MealPlanService mealPlanService;

    @PostMapping("/generate")
    public Result generate(@RequestBody MealPlanGenerateDTO dto) {
        log.info("生成餐计划: {}", dto);
        int count = mealPlanService.generate(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    @GetMapping
    public Result list(@RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                       @RequestParam(defaultValue = "7") int days) {
        return Result.success(mealPlanService.list(startDate, days));
    }

    @DeleteMapping
    public Result clear(@RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(defaultValue = "7") int days) {
        mealPlanService.clear(startDate, days);
        return Result.success();
    }

    @PutMapping("/{id}/adopt")
    public Result adopt(@PathVariable Integer id) {
        mealPlanService.adopt(id);
        return Result.success();
    }

}
