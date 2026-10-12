package com.health.canteen.controller.common;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.dto.CategoryPageQueryDTO;
import com.health.canteen.entity.DishCategory;
import com.health.canteen.result.PageResult;
import com.health.canteen.result.Result;
import com.health.canteen.service.DishCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RequestMapping("/categories")
@RestController("commonDishCategoryController")
public class DishCategoryController {

    @Autowired
    private DishCategoryService dishCategoryService;

    /**
     * 分页查询菜品分类
     */
    @GetMapping
    public Result getCategoryPage(CategoryPageQueryDTO categoryQueryParam) {
        log.info("分页查询菜品分类: {}", categoryQueryParam);
        PageResult<DishCategory> pageResult = dishCategoryService.getCategoryPage(categoryQueryParam);
        return Result.success(pageResult);
    }

    /**
     * 查询启用中的分类，用于下拉选择
     */
    @GetMapping("/enabled")
    public Result getEnabledCategories() {
        log.info("查询启用中的菜品分类");
        return Result.success(dishCategoryService.getEnabledCategories());
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    public Result getCategoryById(@PathVariable Integer id) {
        log.info("根据ID查询菜品分类: {}", id);
        return Result.success(dishCategoryService.getCategoryById(id));
    }

}
