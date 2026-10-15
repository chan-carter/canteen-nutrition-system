package com.health.canteen.controller.merchant;

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
@RestController("merchantReportController")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 菜品销量统计（管理员与本商户可查看）
     */
    @GetMapping("/dishSalesData")
    @RequireRole({2, 3})
    public Result dishSalesData() {
        log.info("查询菜品销量统计数据");
        return Result.success(reportService.getDishSalesData());
    }

}
