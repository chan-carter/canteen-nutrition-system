package com.health.canteen.mapper;

import com.health.canteen.entity.Nutrition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NutritionMapper {

    /**
     * 查询菜品的营养分析结果
     */
    Nutrition getByDishId(Integer dishId);

    /**
     * 保存或更新营养结果（dish_id 唯一）
     */
    void saveOrUpdate(Nutrition nutrition);

    /**
     * 根据菜品ID批量删除营养数据
     */
    void deleteByDishIds(List<Integer> dishIds);

}
