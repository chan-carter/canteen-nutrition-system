package com.health.canteen.controller.common;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.dto.DishAllergenDTO;
import com.health.canteen.result.Result;
import com.health.canteen.service.AllergenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("commonAllergenController")
public class AllergenController {

    @Autowired
    private AllergenService allergenService;

    @GetMapping("/allergens")
    public Result listAll() {
        return Result.success(allergenService.listAll());
    }

    @GetMapping("/dishes/{id}/allergens")
    public Result listByDish(@PathVariable Integer id) {
        return Result.success(allergenService.listByDish(id));
    }

    @PutMapping("/dishes/{id}/allergens")
    @RequireRole({2, 3})
    public Result setDishAllergens(@PathVariable Integer id, @RequestBody DishAllergenDTO dto) {
        log.info("设置菜品过敏原: {}", id);
        allergenService.setDishAllergens(id, dto.getAllergenIds());
        return Result.success();
    }

}
