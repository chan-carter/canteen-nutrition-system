package com.health.canteen.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.DishCategoryMapper;
import com.health.canteen.dto.CategoryPageQueryDTO;
import com.health.canteen.entity.DishCategory;
import com.health.canteen.result.PageResult;
import com.health.canteen.service.DishCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import com.health.canteen.constant.RedisConstant;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DishCategoryServiceImpl implements DishCategoryService {

    @Autowired
    private DishCategoryMapper dishCategoryMapper;

    @Override
    public PageResult<DishCategory> getCategoryPage(CategoryPageQueryDTO categoryQueryParam) {
        PageHelper.startPage(categoryQueryParam.getPage(), categoryQueryParam.getPageSize());
        List<DishCategory> categories = dishCategoryMapper.getCategoryPage(categoryQueryParam);
        Page<DishCategory> page = (Page<DishCategory>) categories;
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Cacheable(cacheNames = RedisConstant.CATEGORY_CACHE, key = "'enabled'")
    @Override
    public List<DishCategory> getEnabledCategories() {
        return dishCategoryMapper.getEnabledCategories();
    }

    @Cacheable(cacheNames = RedisConstant.CATEGORY_CACHE, key = "#id")
    @Override
    public DishCategory getCategoryById(Integer id) {
        DishCategory category = dishCategoryMapper.getCategoryById(id);
        if (category == null) {
            throw new BusinessException("菜品分类不存在");
        }
        return category;
    }

    @CacheEvict(cacheNames = {RedisConstant.CATEGORY_CACHE, RedisConstant.DISH_CACHE}, allEntries = true)
    @Override
    public void addCategory(DishCategory dishCategory) {
        if (isBlank(dishCategory.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        if (dishCategory.getStatus() == null) {
            dishCategory.setStatus(1);
        }
        if (dishCategory.getSortNum() == null) {
            dishCategory.setSortNum(0);
        }
        dishCategory.setCreateTime(LocalDateTime.now());
        dishCategory.setUpdateTime(LocalDateTime.now());
        dishCategoryMapper.addCategory(dishCategory);
    }

    @CacheEvict(cacheNames = {RedisConstant.CATEGORY_CACHE, RedisConstant.DISH_CACHE}, allEntries = true)
    @Override
    public void updateCategory(DishCategory dishCategory) {
        if (dishCategory.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        getCategoryById(dishCategory.getId());
        dishCategory.setUpdateTime(LocalDateTime.now());
        dishCategoryMapper.updateCategory(dishCategory);
    }

    @CacheEvict(cacheNames = {RedisConstant.CATEGORY_CACHE, RedisConstant.DISH_CACHE}, allEntries = true)
    @Override
    public void deleteCategory(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的分类");
        }
        for (Integer id : ids) {
            if (dishCategoryMapper.countDishByCategoryId(id) > 0) {
                throw new BusinessException("该分类下仍存在菜品，无法删除");
            }
        }
        dishCategoryMapper.deleteCategory(ids);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

}
