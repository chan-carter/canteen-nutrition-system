package com.health.canteen.service.impl;

import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.DishMapper;
import com.health.canteen.mapper.RecommendFeedbackMapper;
import com.health.canteen.mapper.RecommendStatMapper;
import com.health.canteen.mapper.RecommendationMapper;
import com.health.canteen.mapper.UserMapper;
import com.health.canteen.context.*;
import com.health.canteen.dto.*;
import com.health.canteen.entity.*;
import com.health.canteen.result.*;
import com.health.canteen.vo.*;
import com.health.canteen.service.RecommendationService;
import com.health.canteen.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import com.health.canteen.constant.RedisConstant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 个性化推荐：按健康目标对候选菜品做区间打分，忌口食材直接过滤
 */
@Slf4j
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final String GOAL_FAT_LOSS = "减脂";
    private static final String GOAL_MUSCLE_GAIN = "增肌";
    private static final String GOAL_BALANCED = "均衡饮食";
    private static final String DEFAULT_GOAL = GOAL_BALANCED;

    /**
     * 参与排序的候选菜品上限
     */
    private static final int CANDIDATE_LIMIT = 100;

    /**
     * 刷新时保存的推荐条数
     */
    private static final int REFRESH_SIZE = 10;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private RecommendFeedbackMapper recommendFeedbackMapper;

    @Autowired
    private RecommendStatMapper recommendStatMapper;

    @Cacheable(cacheNames = RedisConstant.RECOMMEND_CACHE, key = "T(com.health.canteen.utils.CurrentUser).id() + ':'"
            + " + #queryParam.healthGoal + ':' + #queryParam.categoryId + ':' + #queryParam.maxCalories"
            + " + ':' + #queryParam.page + ':' + #queryParam.pageSize")
    @Override
    public PageResult<RecommendDishVO> getRecommendations(RecommendationPageQueryDTO queryParam) {
        User profile = currentUserProfile();
        String goal = resolveGoal(queryParam.getHealthGoal(), profile.getHealthGoal());

        RecommendDishQueryDTO dishQuery = new RecommendDishQueryDTO();
        dishQuery.setCategoryId(queryParam.getCategoryId());
        dishQuery.setMaxCalories(queryParam.getMaxCalories());
        dishQuery.setLimitSize(CANDIDATE_LIMIT);
        List<RecommendDishVO> candidates = dishMapper.getRecommendingDishes(dishQuery);
        if (candidates == null || candidates.isEmpty()) {
            return new PageResult<>(0L, new ArrayList<>());
        }

        List<String> taboos = splitKeywords(profile.getTaboo());
        List<String> preferences = splitKeywords(profile.getPreference());
        List<RecommendDishVO> scored = new ArrayList<>();
        for (RecommendDishVO item : candidates) {
            if (hitsTaboo(item, taboos)) {
                continue;
            }
            score(item, goal, preferences);
            scored.add(item);
        }
        personalize(scored, CurrentUser.id());
        scored.sort(Comparator.comparing(RecommendDishVO::getScore, Comparator.reverseOrder())
                .thenComparing(RecommendDishVO::getDishId));

        int page = queryParam.getPage() == null || queryParam.getPage() < 1 ? 1 : queryParam.getPage();
        int pageSize = queryParam.getPageSize() == null || queryParam.getPageSize() < 1 ? 10 : queryParam.getPageSize();
        int from = (page - 1) * pageSize;
        // 必须是独立的ArrayList：subList返回的是视图，序列化成 java.util.ArrayList$SubList 后无法反序列化，缓存会读不回来
        List<RecommendDishVO> rows = from >= scored.size()
                ? new ArrayList<>()
                : new ArrayList<>(scored.subList(from, Math.min(from + pageSize, scored.size())));
        log.info("生成推荐结果, 目标: {}, 候选: {}, 命中忌口过滤后: {}", goal, candidates.size(), scored.size());
        return new PageResult<>((long) scored.size(), rows);
    }

    @CacheEvict(cacheNames = RedisConstant.RECOMMEND_CACHE, allEntries = true)
    @Override
    @Transactional
    public int refresh() {
        User profile = currentUserProfile();
        RecommendationPageQueryDTO queryParam = new RecommendationPageQueryDTO();
        queryParam.setHealthGoal(profile.getHealthGoal());
        queryParam.setPage(1);
        queryParam.setPageSize(REFRESH_SIZE);
        List<RecommendDishVO> items = getRecommendations(queryParam).getRows();
        if (items.isEmpty()) {
            throw new BusinessException("暂无符合条件的推荐菜品，请先完善菜品营养分析数据");
        }

        Integer userId = CurrentUser.id();
        recommendationMapper.deleteByUserId(userId);
        List<Recommendation> records = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (RecommendDishVO item : items) {
            Recommendation record = new Recommendation();
            record.setUserId(userId);
            record.setDishId(item.getDishId());
            record.setReason(item.getReason());
            record.setScore(item.getScore());
            record.setHealthGoal(resolveGoal(null, profile.getHealthGoal()));
            record.setCreateTime(now);
            records.add(record);
        }
        recommendationMapper.batchAdd(records);
        log.info("推荐结果已刷新并保存, 用户: {}, 条数: {}", userId, records.size());
        return records.size();
    }

    /**
     * 在规则打分基础上叠加：反馈（踩过滤/赞加分）、协同过滤共现加分、餐段场景加分
     */
    private void personalize(List<RecommendDishVO> items, Integer userId) {
        if (items.isEmpty()) {
            return;
        }
        Map<Integer, Integer> feedback = new HashMap<>();
        for (RecommendFeedback f : recommendFeedbackMapper.listByUser(userId)) {
            feedback.put(f.getDishId(), f.getAction());
        }
        Map<Integer, Integer> cooc = new HashMap<>();
        int maxCnt = 0;
        for (Map<String, Object> row : recommendStatMapper.listCooccurringDishes(userId, 20)) {
            Object did = row.get("dishId");
            Object cnt = row.get("cnt");
            if (did == null || cnt == null) {
                continue;
            }
            int c = ((Number) cnt).intValue();
            cooc.put(((Number) did).intValue(), c);
            maxCnt = Math.max(maxCnt, c);
        }
        int hour = LocalTime.now().getHour();
        boolean breakfast = hour < 10;
        boolean dinner = hour >= 17 && hour < 21;

        Iterator<RecommendDishVO> it = items.iterator();
        while (it.hasNext()) {
            RecommendDishVO item = it.next();
            Integer flag = feedback.get(item.getDishId());
            if (flag != null && flag == -1) {
                it.remove();
                continue;
            }
            double value = item.getScore().doubleValue();
            StringBuilder extra = new StringBuilder();
            if (flag != null && flag == 1) {
                value += 8;
                extra.append("，你标过喜欢");
            }
            Integer cnt = cooc.get(item.getDishId());
            if (cnt != null && maxCnt > 0) {
                value += 12.0 * cnt / maxCnt;
                extra.append("，和你常点的菜很搭");
            }
            double cal = doubleOf(item.getCalorie());
            if (breakfast && cal <= 300) {
                value += 4;
                extra.append("，适合早餐");
            } else if (dinner && cal <= 400) {
                value += 3;
                extra.append("，晚餐宜清淡");
            }
            item.setScore(clamp(value));
            if (extra.length() > 0 && item.getReason() != null) {
                item.setReason(item.getReason() + extra);
            }
        }
    }

    @CacheEvict(cacheNames = RedisConstant.RECOMMEND_CACHE, allEntries = true)
    @Override
    public void feedback(Integer dishId, Integer action, String reason) {
        if (dishId == null || action == null || (action != 1 && action != -1)) {
            throw new BusinessException("反馈参数不正确");
        }
        RecommendFeedback feedback = new RecommendFeedback();
        feedback.setUserId(CurrentUser.id());
        feedback.setDishId(dishId);
        feedback.setAction(action);
        feedback.setReason(reason);
        feedback.setCreateTime(LocalDateTime.now());
        recommendFeedbackMapper.upsert(feedback);
        log.info("推荐反馈, 用户: {}, 菜品: {}, 动作: {}", feedback.getUserId(), dishId, action);
    }

    /**
     * 目标区间打分，基础分50分，命中目标区间逐项加分，得分越高表示越贴合该健康目标
     */
    private void score(RecommendDishVO item, String goal, List<String> preferences) {
        List<String> reasons = new ArrayList<>();
        double value = 50;
        double calorie = doubleOf(item.getCalorie());
        double protein = doubleOf(item.getProtein());
        double fat = doubleOf(item.getFat());
        double carbohydrate = doubleOf(item.getCarbohydrate());
        double fiber = doubleOf(item.getFiber());
        double sodium = doubleOf(item.getSodium());

        switch (goal) {
            case GOAL_FAT_LOSS:
                if (calorie <= 150) {
                    value += 20;
                    reasons.add("热量较低");
                } else if (calorie <= 300) {
                    value += 14;
                    reasons.add("热量控制较好");
                } else if (calorie > 450) {
                    value -= 18;
                }
                if (fat <= 5) {
                    value += 12;
                    reasons.add("脂肪含量低");
                } else if (fat > 25) {
                    value -= 10;
                }
                if (protein >= 10) {
                    value += 10;
                    reasons.add("含有一定蛋白质");
                }
                if (fiber >= 3) {
                    value += 5;
                    reasons.add("膳食纤维较丰富");
                }
                if (sodium <= 500) {
                    value += 4;
                }
                break;
            case GOAL_MUSCLE_GAIN:
                if (protein >= 20) {
                    value += 22;
                    reasons.add("蛋白质含量高，适合增肌期补充");
                } else if (protein >= 12) {
                    value += 14;
                    reasons.add("蛋白质含量较好");
                } else if (protein < 6) {
                    value -= 15;
                }
                if (calorie >= 250 && calorie <= 550) {
                    value += 12;
                    reasons.add("热量区间适合正餐搭配");
                }
                if (fat <= 15) {
                    value += 6;
                }
                if (carbohydrate >= 20) {
                    value += 6;
                    reasons.add("碳水供能充足");
                }
                break;
            default:
                if (calorie >= 200 && calorie <= 500) {
                    value += 12;
                    reasons.add("热量适中");
                } else {
                    value -= 8;
                }
                if (protein >= 8 && protein <= 30) {
                    value += 12;
                    reasons.add("蛋白质含量适中");
                }
                if (fat >= 3 && fat <= 20) {
                    value += 10;
                    reasons.add("脂肪比例合理");
                }
                if (carbohydrate >= 15 && carbohydrate <= 65) {
                    value += 8;
                }
                if (fiber >= 2) {
                    value += 6;
                    reasons.add("含有一定膳食纤维");
                }
                if (sodium <= 800) {
                    value += 6;
                    reasons.add("钠含量在合理范围");
                } else {
                    value -= 6;
                }
                break;
        }

        if (item.getAvgRating() != null && item.getAvgRating().doubleValue() >= 4.5) {
            value += 4;
            reasons.add("学生评价较好");
        }
        if (hitsAny(item, preferences)) {
            value += 6;
            reasons.add("贴合已设置的饮食偏好");
        }

        item.setScore(clamp(value));
        item.setReason(buildReason(reasons, item, goal));
    }

    private String buildReason(List<String> reasons, RecommendDishVO item, String goal) {
        if (reasons.isEmpty()) {
            reasons.add("营养结构与" + goal + "目标基本匹配");
        }
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(reasons.size(), 3);
        for (int i = 0; i < limit; i++) {
            sb.append(i == 0 ? "" : "，").append(reasons.get(i));
        }
        sb.append("；热量约").append(item.getCalorie()).append("kcal，蛋白质")
                .append(item.getProtein()).append("g，价格").append(item.getPrice()).append("元");
        if (item.getStallName() != null) {
            sb.append("，来自").append(item.getStallName());
        }
        return sb.toString();
    }

    private boolean hitsTaboo(RecommendDishVO item, List<String> taboos) {
        return hitsAny(item, taboos);
    }

    /**
     * 忌口与偏好都通过菜品名称和主要食材做关键词匹配
     */
    private boolean hitsAny(RecommendDishVO item, List<String> keywords) {
        if (keywords.isEmpty()) {
            return false;
        }
        String text = (item.getDishName() == null ? "" : item.getDishName())
                + (item.getIngredients() == null ? "" : item.getIngredients());
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private List<String> splitKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return keywords;
        }
        for (String item : text.split("[、，,;；/\\s]+")) {
            String keyword = item.trim();
            //偏好中的“清淡”“高蛋白”等描述性词语只用于加分，不作为过滤条件
            if (!keyword.isEmpty() && keyword.length() >= 2) {
                keywords.add(keyword);
            }
        }
        return keywords;
    }

    private String resolveGoal(String paramGoal, String userGoal) {
        String goal = paramGoal != null && !paramGoal.trim().isEmpty() ? paramGoal : userGoal;
        if (goal == null || goal.trim().isEmpty()) {
            return DEFAULT_GOAL;
        }
        goal = goal.trim();
        if (goal.contains("减脂") || goal.contains("控重") || goal.contains("减肥")) {
            return GOAL_FAT_LOSS;
        }
        if (goal.contains("增肌") || goal.contains("健身")) {
            return GOAL_MUSCLE_GAIN;
        }
        return GOAL_BALANCED;
    }

    private User currentUserProfile() {
        User profile = userMapper.getUserById(CurrentUser.id());
        if (profile == null) {
            throw new BusinessException("用户不存在，请重新登录");
        }
        return profile;
    }

    private double doubleOf(BigDecimal value) {
        return value == null ? 0 : value.doubleValue();
    }

    private BigDecimal clamp(double value) {
        double limited = Math.max(0, Math.min(100, value));
        return BigDecimal.valueOf(limited).setScale(1, RoundingMode.HALF_UP);
    }

}
