package com.health.canteen.controller.admin;

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
@RestController("adminDishCategoryController")
public class DishCategoryController {

    @Autowired
    private DishCategoryService dishCategoryService;

    /**
     * 新增分类（仅管理员）
     */
    @PostMapping
    @RequireRole(3)
    public Result addCategory(@RequestBody DishCategory dishCategory) {
        log.info("新增菜品分类: {}", dishCategory);
        dishCategoryService.addCategory(dishCategory);
        return Result.success();
    }

    /**
     * 修改分类（仅管理员）
     */
    @PutMapping
    @RequireRole(3)
    public Result updateCategory(@RequestBody DishCategory dishCategory) {
        log.info("修改菜品分类: {}", dishCategory);
        dishCategoryService.updateCategory(dishCategory);
        return Result.success();
    }

    /**
     * 删除分类（仅管理员）
     */
    @DeleteMapping("/{ids}")
    @RequireRole(3)
    public Result deleteCategory(@PathVariable List<Integer> ids) {
        log.info("删除菜品分类: {}", ids);
        dishCategoryService.deleteCategory(ids);
        return Result.success();
    }

}
