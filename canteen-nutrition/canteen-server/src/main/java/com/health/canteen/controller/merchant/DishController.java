package com.health.canteen.controller.merchant;

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
@RestController("merchantDishController")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     */
    @PostMapping
    @RequireRole({2, 3})
    @Log(module = "菜品管理", operation = "新增菜品")
    public Result addDish(@RequestBody Dish dish) {
        log.info("新增菜品: {}", dish.getName());
        dishService.addDish(dish);
        return Result.success();
    }

    /**
     * 修改菜品
     */
    @PutMapping
    @RequireRole({2, 3})
    @Log(module = "菜品管理", operation = "修改菜品")
    public Result updateDish(@RequestBody Dish dish) {
        log.info("修改菜品: {}", dish.getId());
        dishService.updateDish(dish);
        return Result.success();
    }

    /**
     * 删除菜品
     */
    @DeleteMapping("/{ids}")
    @RequireRole({2, 3})
    public Result deleteDish(@PathVariable List<Integer> ids) {
        log.info("删除菜品: {}", ids);
        dishService.deleteDish(ids);
        return Result.success();
    }

    /**
     * 菜品上下架
     */
    @PutMapping("/{id}/status")
    @RequireRole({2, 3})
    public Result updateStatus(@PathVariable Integer id, @RequestBody Dish dish) {
        log.info("菜品上下架: {}, {}", id, dish.getStatus());
        dishService.updateStatus(id, dish.getStatus());
        return Result.success();
    }

}
