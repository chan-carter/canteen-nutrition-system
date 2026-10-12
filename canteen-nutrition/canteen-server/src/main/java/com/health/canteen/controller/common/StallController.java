package com.health.canteen.controller.common;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.result.PageResult;
import com.health.canteen.result.Result;
import com.health.canteen.entity.Stall;
import com.health.canteen.dto.StallPageQueryDTO;
import com.health.canteen.service.StallService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RequestMapping("/stalls")
@RestController("commonStallController")
public class StallController {

    @Autowired
    private StallService stallService;

    /**
     * 分页查询档口信息
     */
    @GetMapping
    public Result getStallPage(StallPageQueryDTO stallQueryParam) {
        log.info("分页查询档口信息: {}", stallQueryParam);
        PageResult<Stall> pageResult = stallService.getStallPage(stallQueryParam);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询档口
     */
    @GetMapping("/{id}")
    public Result getStallById(@PathVariable Integer id) {
        log.info("根据ID查询档口: {}", id);
        return Result.success(stallService.getStallById(id));
    }

}
