package com.health.canteen.controller.merchant;

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
@RestController("merchantStallController")
public class StallController {

    @Autowired
    private StallService stallService;

    /**
     * 新增档口
     */
    @PostMapping
    @RequireRole({2, 3})
    public Result addStall(@RequestBody Stall stall) {
        log.info("新增档口: {}", stall);
        stallService.addStall(stall);
        return Result.success();
    }

    /**
     * 修改档口
     */
    @PutMapping
    @RequireRole({2, 3})
    public Result updateStall(@RequestBody Stall stall) {
        log.info("修改档口: {}", stall);
        stallService.updateStall(stall);
        return Result.success();
    }

    /**
     * 修改档口营业状态
     */
    @PutMapping("/{id}/status")
    @RequireRole({2, 3})
    public Result updateStatus(@PathVariable Integer id, @RequestBody Stall stall) {
        log.info("修改档口状态: {}, {}", id, stall.getStatus());
        stallService.updateStatus(id, stall.getStatus());
        return Result.success();
    }

    /**
     * 删除档口
     */
    @DeleteMapping("/{ids}")
    @RequireRole({2, 3})
    public Result deleteStall(@PathVariable List<Integer> ids) {
        log.info("删除档口: {}", ids);
        stallService.deleteStall(ids);
        return Result.success();
    }

}
