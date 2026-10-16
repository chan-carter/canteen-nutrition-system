package com.health.canteen.service;

import com.health.canteen.dto.MealPlanGenerateDTO;
import com.health.canteen.entity.MealPlan;

import java.time.LocalDate;
import java.util.List;

public interface MealPlanService {

    int generate(MealPlanGenerateDTO dto);

    List<MealPlan> list(LocalDate startDate, int days);

    void clear(LocalDate startDate, int days);

    void adopt(Integer id);
}
