package com.health.canteen.mapper;

import com.health.canteen.entity.Allergen;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AllergenMapper {

    List<Allergen> listAll();

    List<Allergen> listByDishId(@Param("dishId") Integer dishId);

    void deleteByDishId(@Param("dishId") Integer dishId);

    void batchInsert(@Param("dishId") Integer dishId, @Param("allergenIds") List<Integer> allergenIds);
}
