package com.health.canteen.mapper;

import com.health.canteen.dto.CategoryPageQueryDTO;
import com.health.canteen.entity.DishCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishCategoryMapper {

    /**
     * 条件分页查询菜品分类
     */
    List<DishCategory> getCategoryPage(CategoryPageQueryDTO categoryQueryParam);

    /**
     * 查询全部启用的分类（下拉框数据）
     */
    List<DishCategory> getEnabledCategories();

    /**
     * 根据ID查询分类
     */
    DishCategory getCategoryById(Integer id);

    /**
     * 新增分类
     */
    void addCategory(DishCategory dishCategory);

    /**
     * 修改分类
     */
    void updateCategory(DishCategory dishCategory);

    /**
     * 批量删除分类
     */
    void deleteCategory(List<Integer> ids);

    /**
     * 统计分类下的菜品数量
     */
    Integer countDishByCategoryId(Integer categoryId);

}
