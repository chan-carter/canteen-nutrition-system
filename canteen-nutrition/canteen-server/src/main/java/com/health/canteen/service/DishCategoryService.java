package com.health.canteen.service;

import com.health.canteen.dto.CategoryPageQueryDTO;
import com.health.canteen.entity.DishCategory;
import com.health.canteen.result.PageResult;

import java.util.List;

public interface DishCategoryService {

    PageResult<DishCategory> getCategoryPage(CategoryPageQueryDTO categoryQueryParam);

    List<DishCategory> getEnabledCategories();

    DishCategory getCategoryById(Integer id);

    void addCategory(DishCategory dishCategory);

    void updateCategory(DishCategory dishCategory);

    void deleteCategory(List<Integer> ids);

}
