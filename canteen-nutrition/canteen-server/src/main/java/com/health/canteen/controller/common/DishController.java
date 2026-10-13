package com.health.canteen.controller.common;

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
@RestController("commonDishController")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 分页查询菜品
     */
    @GetMapping
    public Result getDishPage(DishPageQueryDTO dishQueryParam) {
        log.info("分页查询菜品: {}", dishQueryParam);
        PageResult<Dish> pageResult = dishService.getDishPage(dishQueryParam);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询菜品
     */
    @GetMapping("/{id}")
    public Result getDishById(@PathVariable Integer id) {
        log.info("根据ID查询菜品: {}", id);
        return Result.success(dishService.getDishById(id));
    }

}
