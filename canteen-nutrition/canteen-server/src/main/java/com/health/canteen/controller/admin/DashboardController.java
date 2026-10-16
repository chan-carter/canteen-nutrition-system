package com.health.canteen.controller.admin;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.mapper.DashboardMapper;
import com.health.canteen.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局数据大屏（仅管理员）
 */
@Slf4j
@RequestMapping("/report")
@RestController("adminDashboardController")
@RequireRole(3)
public class DashboardController {

    @Autowired
    private DashboardMapper dashboardMapper;

    @GetMapping("/dashboard")
    public Result dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("global", dashboardMapper.globalStats());
        data.put("categoryDist", dashboardMapper.categoryDist());
        data.put("topDishes", dashboardMapper.topDishes());
        data.put("calorieDist", dashboardMapper.calorieDist());
        data.put("ai", dashboardMapper.aiStats());
        data.put("health", dashboardMapper.healthStats());
        data.put("orderStatusDist", dashboardMapper.orderStatusDist());
        return Result.success(data);
    }

}
