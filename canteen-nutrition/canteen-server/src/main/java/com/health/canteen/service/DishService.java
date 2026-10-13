package com.health.canteen.service;

import com.health.canteen.entity.Dish;
import com.health.canteen.dto.DishPageQueryDTO;
import com.health.canteen.result.PageResult;

import java.util.List;

public interface DishService {

    PageResult<Dish> getDishPage(DishPageQueryDTO dishQueryParam);

    Dish getDishById(Integer id);

    void addDish(Dish dish);

    void updateDish(Dish dish);

    void updateStatus(Integer id, Integer status);

    void audit(Integer id, Integer auditStatus, String auditRemark);

    void deleteDish(List<Integer> ids);

}
