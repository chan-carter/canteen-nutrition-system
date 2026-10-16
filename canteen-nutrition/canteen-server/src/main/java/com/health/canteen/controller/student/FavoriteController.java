package com.health.canteen.controller.student;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.result.Result;
import com.health.canteen.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生端菜品收藏（仅学生）
 */
@Slf4j
@RequestMapping("/favorites")
@RestController("studentFavoriteController")
@RequireRole(1)
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public Result list() {
        return Result.success(favoriteService.list());
    }

    @PostMapping("/{dishId}")
    public Result add(@PathVariable Integer dishId) {
        favoriteService.add(dishId);
        return Result.success();
    }

    @DeleteMapping("/{dishId}")
    public Result cancel(@PathVariable Integer dishId) {
        favoriteService.cancel(dishId);
        return Result.success();
    }

}
