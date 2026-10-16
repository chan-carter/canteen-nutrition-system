package com.health.canteen.mapper;

import com.health.canteen.entity.MealPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MealPlanMapper {

    void deleteByUserRange(@Param("userId") Integer userId,
                           @Param("start") LocalDate start, @Param("end") LocalDate end);

    void batchInsert(@Param("plans") List<MealPlan> plans);

    List<MealPlan> listByUserRange(@Param("userId") Integer userId,
                                   @Param("start") LocalDate start, @Param("end") LocalDate end);

    void markAdopted(@Param("id") Integer id, @Param("userId") Integer userId);
}
