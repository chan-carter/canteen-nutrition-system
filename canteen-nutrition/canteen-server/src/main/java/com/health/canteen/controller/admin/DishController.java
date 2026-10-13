package com.health.canteen.controller.admin;

import com.health.canteen.annotation.Log;
import com.health.canteen.annotation.RequireRole;
import com.health.canteen.dto.DishAuditDTO;
import com.health.canteen.entity.Dish;
import com.health.canteen.dto.DishPageQueryDTO;
import com.health.canteen.result.PageResult;
import com.health.canteen.result.Result;
import com.health.canteen.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RequestMapping("/dishes")
@RestController("adminDishController")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 菜品审核（仅管理员）
     */
    @PutMapping("/{id}/audit")
    @RequireRole(3)
    @Log(module = "菜品管理", operation = "审核菜品")
    public Result audit(@PathVariable Integer id, @RequestBody DishAuditDTO auditParam) {
        log.info("菜品审核: {}, {}", id, auditParam);
        dishService.audit(id, auditParam.getAuditStatus(), auditParam.getAuditRemark());
        return Result.success();
    }

}
