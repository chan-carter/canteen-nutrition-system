package com.health.canteen.service.impl;

import com.health.canteen.dto.MealPlanGenerateDTO;
import com.health.canteen.dto.RecommendationPageQueryDTO;
import com.health.canteen.entity.HealthProfile;
import com.health.canteen.entity.MealPlan;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.HealthProfileMapper;
import com.health.canteen.mapper.MealPlanMapper;
import com.health.canteen.service.MealPlanService;
import com.health.canteen.service.RecommendationService;
import com.health.canteen.utils.CurrentUser;
import com.health.canteen.vo.RecommendDishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 周期餐计划：按当前学生的健康目标取推荐候选，轮流铺到每天的早/中/晚三餐。
 */
@Slf4j
@Service
public class MealPlanServiceImpl implements MealPlanService {

    private static final String[] MEALS = {"早餐", "午餐", "晚餐"};
    private static final int CANDIDATE_SIZE = 30;

    @Autowired
    private MealPlanMapper mealPlanMapper;

    @Autowired
    private HealthProfileMapper healthProfileMapper;

    @Autowired
    private RecommendationService recommendationService;

    @Override
    @Transactional
    public int generate(MealPlanGenerateDTO dto) {
        Integer userId = CurrentUser.id();
        int days = dto.getDays() == null ? 7 : Math.max(1, Math.min(dto.getDays(), 14));
        LocalDate start = dto.getStartDate() == null ? LocalDate.now() : dto.getStartDate();
        LocalDate end = start.plusDays(days - 1L);

        HealthProfile profile = healthProfileMapper.getByUserId(userId);
        RecommendationPageQueryDTO query = new RecommendationPageQueryDTO();
        query.setPage(1);
        query.setPageSize(CANDIDATE_SIZE);
        if (profile != null) {
            query.setHealthGoal(profile.getHealthGoal());
        }
        List<RecommendDishVO> candidates = recommendationService.getRecommendations(query).getRows();
        if (candidates == null || candidates.isEmpty()) {
            throw new BusinessException("暂无可用于生成餐计划的推荐菜品，请先完善菜品营养数据");
        }

        mealPlanMapper.deleteByUserRange(userId, start, end);
        LocalDateTime now = LocalDateTime.now();
        List<MealPlan> plans = new ArrayList<>();
        int idx = 0;
        for (int d = 0; d < days; d++) {
            LocalDate day = start.plusDays(d);
            for (String meal : MEALS) {
                RecommendDishVO dish = candidates.get(idx % candidates.size());
                idx++;
                MealPlan plan = new MealPlan();
                plan.setUserId(userId);
                plan.setPlanDate(day);
                plan.setMealType(meal);
                plan.setDishId(dish.getDishId());
                plan.setDishName(dish.getDishName());
                plan.setPlannedCalorie(dish.getCalorie());
                plan.setAdopted(0);
                plan.setCreateTime(now);
                plan.setUpdateTime(now);
                plans.add(plan);
            }
        }
        mealPlanMapper.batchInsert(plans);
        log.info("生成餐计划, 用户: {}, {}天, 共{}条", userId, days, plans.size());
        return plans.size();
    }

    @Override
    public List<MealPlan> list(LocalDate startDate, int days) {
        LocalDate start = startDate == null ? LocalDate.now() : startDate;
        int span = days <= 0 ? 7 : Math.min(days, 14);
        return mealPlanMapper.listByUserRange(CurrentUser.id(), start, start.plusDays(span - 1L));
    }

    @Override
    public void clear(LocalDate startDate, int days) {
        LocalDate start = startDate == null ? LocalDate.now() : startDate;
        int span = days <= 0 ? 7 : Math.min(days, 14);
        mealPlanMapper.deleteByUserRange(CurrentUser.id(), start, start.plusDays(span - 1L));
    }

    @Override
    public void adopt(Integer id) {
        mealPlanMapper.markAdopted(id, CurrentUser.id());
    }
}
