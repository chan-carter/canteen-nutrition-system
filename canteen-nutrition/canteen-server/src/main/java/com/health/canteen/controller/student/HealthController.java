package com.health.canteen.controller.student;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.dto.DietRecordDTO;
import com.health.canteen.dto.HealthProfileDTO;
import com.health.canteen.dto.WeightLogDTO;
import com.health.canteen.result.Result;
import com.health.canteen.service.HealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 学生端营养健康追踪（仅学生）
 */
@Slf4j
@RequestMapping("/health")
@RestController("studentHealthController")
@RequireRole(1)
public class HealthController {

    @Autowired
    private HealthService healthService;

    @GetMapping("/profile")
    public Result getProfile() {
        return Result.success(healthService.getProfile());
    }

    @PostMapping("/profile")
    public Result saveProfile(@RequestBody HealthProfileDTO dto) {
        log.info("保存身体档案");
        return Result.success(healthService.saveProfile(dto));
    }

    @GetMapping("/daily")
    public Result dailyIntake(@RequestParam(required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(healthService.dailyIntake(date));
    }

    @GetMapping("/diet")
    public Result listDiet(@RequestParam(required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(healthService.listDiet(date));
    }

    @PostMapping("/diet")
    public Result addDiet(@RequestBody DietRecordDTO dto) {
        healthService.addDiet(dto);
        return Result.success();
    }

    @DeleteMapping("/diet/{id}")
    public Result deleteDiet(@PathVariable Integer id) {
        healthService.deleteDiet(id);
        return Result.success();
    }

    @GetMapping("/weight/trend")
    public Result weightTrend(@RequestParam(defaultValue = "30") int days) {
        return Result.success(healthService.weightTrend(days));
    }

    @PostMapping("/weight")
    public Result addWeight(@RequestBody WeightLogDTO dto) {
        healthService.addWeight(dto);
        return Result.success();
    }

}
