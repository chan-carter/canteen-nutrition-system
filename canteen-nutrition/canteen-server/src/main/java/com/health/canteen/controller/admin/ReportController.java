package com.health.canteen.controller.admin;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.result.Result;
import com.health.canteen.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/report")
@RestController("adminReportController")
@RequireRole(3)
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 菜品分类数量统计
     */
    @GetMapping("/dishCategoryData")
    public Result dishCategoryData() {
        log.info("查询菜品分类统计数据");
        return Result.success(reportService.getDishCategoryData());
    }

    /**
     * 学生健康目标分布
     */
    @GetMapping("/healthGoalData")
    public Result healthGoalData() {
        log.info("查询健康目标统计数据");
        return Result.success(reportService.getHealthGoalData());
    }

    /**
     * 订单趋势统计
     */
    @GetMapping("/orderData")
    public Result orderData() {
        log.info("查询订单统计数据");
        return Result.success(reportService.getOrderData());
    }

    /**
     * 评价评分分布
     */
    @GetMapping("/reviewData")
    public Result reviewData() {
        log.info("查询评价统计数据");
        return Result.success(reportService.getReviewData());
    }

    /**
     * 菜品被推荐次数统计
     */
    @GetMapping("/recommendHotData")
    public Result recommendHotData() {
        log.info("查询推荐统计数据");
        return Result.success(reportService.getRecommendHotData());
    }

    /**
     * AI接口调用情况统计
     */
    @GetMapping("/aiCallData")
    public Result aiCallData() {
        log.info("查询AI调用统计数据");
        return Result.success(reportService.getAiCallData());
    }

    /**
     * 首页总览数据
     */
    @GetMapping("/overviewData")
    public Result overviewData() {
        log.info("查询系统总览数据");
        return Result.success(reportService.getOverviewData());
    }

}
