package com.health.canteen.service;

import com.health.canteen.context.BaseContext;
import com.health.canteen.context.LoginUser;
import com.health.canteen.dto.RecommendationPageQueryDTO;
import com.health.canteen.entity.RecommendFeedback;
import com.health.canteen.entity.User;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.DishMapper;
import com.health.canteen.mapper.RecommendFeedbackMapper;
import com.health.canteen.mapper.RecommendStatMapper;
import com.health.canteen.mapper.RecommendationMapper;
import com.health.canteen.mapper.UserMapper;
import com.health.canteen.result.PageResult;
import com.health.canteen.service.impl.RecommendationServiceImpl;
import com.health.canteen.vo.RecommendDishVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 推荐排序的回归用例。刻意把所有候选热量都放在 400 kcal 以上，避开早餐/晚餐场景加分，
 * 这样分数只由目标区间打分与反馈、协同过滤决定，结果与运行时刻无关。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("个性化推荐排序")
class RecommendationRankingTest {

    @Mock
    private DishMapper dishMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RecommendationMapper recommendationMapper;

    @Mock
    private RecommendFeedbackMapper recommendFeedbackMapper;

    @Mock
    private RecommendStatMapper recommendStatMapper;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @BeforeEach
    void login() {
        BaseContext.set(new LoginUser(1, "zhangsan", 1, null));
    }

    @AfterEach
    void logout() {
        BaseContext.remove();
    }

    /**
     * 只有走完整推荐链路的用例才需要账号资料，反馈用例不查库，所以按需打桩
     */
    private void givenProfile(String goal, String taboo) {
        User user = new User();
        user.setId(1);
        user.setHealthGoal(goal);
        user.setTaboo(taboo);
        when(userMapper.getUserById(1)).thenReturn(user);
    }

    private RecommendDishVO dish(int id, String name, String ingredients, String calorie) {
        RecommendDishVO vo = new RecommendDishVO();
        vo.setDishId(id);
        vo.setDishName(name);
        vo.setIngredients(ingredients);
        vo.setPrice(new BigDecimal("10"));
        vo.setStallName("家常炒菜窗口");
        vo.setCalorie(new BigDecimal(calorie));
        vo.setProtein(BigDecimal.ZERO);
        vo.setFat(new BigDecimal("10"));
        vo.setCarbohydrate(BigDecimal.ZERO);
        vo.setFiber(BigDecimal.ZERO);
        vo.setSodium(new BigDecimal("900"));
        return vo;
    }

    private RecommendFeedback feedback(int dishId, int action) {
        RecommendFeedback f = new RecommendFeedback();
        f.setUserId(1);
        f.setDishId(dishId);
        f.setAction(action);
        return f;
    }

    private RecommendationPageQueryDTO query(int page, int pageSize) {
        RecommendationPageQueryDTO dto = new RecommendationPageQueryDTO();
        dto.setHealthGoal("减脂");
        dto.setPage(page);
        dto.setPageSize(pageSize);
        return dto;
    }

    @Test
    @DisplayName("忌口过滤 + 踩删 + 赞加分 + 协同过滤加分，按分数降序")
    void ranksByGoalFeedbackAndCooccurrence() {
        givenProfile("减脂", "香菜");
        when(dishMapper.getRecommendingDishes(any())).thenReturn(new ArrayList<>(List.of(
                dish(201, "红烧狮子头", "猪肉、荸荠", "410"),
                dish(202, "香菜拌牛肉", "牛肉、香菜", "415"),
                dish(203, "梅菜扣肉", "五花肉、梅菜", "420"),
                dish(204, "辣子鸡", "鸡块、干辣椒", "430"),
                dish(205, "糖醋排骨", "排骨、白糖", "440"))));
        when(recommendFeedbackMapper.listByUser(1)).thenReturn(List.of(feedback(201, 1), feedback(204, -1)));
        Map<String, Object> cooc = new HashMap<>();
        cooc.put("dishId", 203);
        cooc.put("cnt", 5);
        when(recommendStatMapper.listCooccurringDishes(1, 20)).thenReturn(List.of(cooc));

        PageResult<RecommendDishVO> result = recommendationService.getRecommendations(query(1, 10));

        // 202 命中忌口「香菜」，204 被踩过，都不该出现
        assertThat(result.getRows()).extracting(RecommendDishVO::getDishId).containsExactly(203, 201, 205);
        assertThat(result.getTotal()).isEqualTo(3);
        // 基础分 50：203 吃到协同过滤满分 +12，201 被赞 +8
        assertThat(result.getRows().get(0).getScore()).isEqualByComparingTo("62.0");
        assertThat(result.getRows().get(1).getScore()).isEqualByComparingTo("58.0");
        assertThat(result.getRows().get(2).getScore()).isEqualByComparingTo("50.0");
        assertThat(result.getRows().get(0).getReason()).contains("和你常点的菜很搭");
        assertThat(result.getRows().get(1).getReason()).contains("你标过喜欢");
        assertThat(result.getRows().get(2).getReason()).doesNotContain("和你常点的菜很搭");
    }

    @Test
    @DisplayName("翻页越界返回空列表，且必须是可反序列化的 ArrayList")
    void pageBeyondRangeReturnsPlainList() {
        givenProfile("减脂", "香菜");
        when(dishMapper.getRecommendingDishes(any())).thenReturn(new ArrayList<>(List.of(
                dish(201, "红烧狮子头", "猪肉", "410"))));
        when(recommendFeedbackMapper.listByUser(1)).thenReturn(List.of());
        when(recommendStatMapper.listCooccurringDishes(1, 20)).thenReturn(List.of());

        PageResult<RecommendDishVO> second = recommendationService.getRecommendations(query(2, 10));

        assertThat(second.getRows()).isEmpty();
        assertThat(second.getRows()).isExactlyInstanceOf(ArrayList.class);
        assertThat(second.getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("没有候选菜品时直接返回空结果，不报错")
    void emptyCandidates() {
        givenProfile("减脂", "香菜");
        when(dishMapper.getRecommendingDishes(any())).thenReturn(List.of());

        PageResult<RecommendDishVO> result = recommendationService.getRecommendations(query(1, 10));

        assertThat(result.getTotal()).isZero();
        assertThat(result.getRows()).isEmpty();
    }

    @Test
    @DisplayName("反馈动作只接受 1/-1，合法反馈写入 recommend_feedback")
    void validatesFeedbackAction() {
        assertThatThrownBy(() -> recommendationService.feedback(101, 2, "太咸"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("反馈参数");

        recommendationService.feedback(101, -1, "太咸");

        ArgumentCaptor<RecommendFeedback> captor = ArgumentCaptor.forClass(RecommendFeedback.class);
        verify(recommendFeedbackMapper).upsert(captor.capture());
        assertThat(captor.getValue().getDishId()).isEqualTo(101);
        assertThat(captor.getValue().getAction()).isEqualTo(-1);
        assertThat(captor.getValue().getUserId()).isEqualTo(1);
    }

    @Test
    @DisplayName("无候选时刷新推荐记录会被拒绝，避免写空表")
    void refreshRejectsWhenNothingToSave() {
        givenProfile("减脂", "香菜");
        when(dishMapper.getRecommendingDishes(any())).thenReturn(List.of());

        assertThatThrownBy(() -> recommendationService.refresh())
                .isInstanceOf(BusinessException.class).hasMessageContaining("暂无符合条件");
        verify(recommendationMapper, never()).deleteByUserId(anyInt());
    }

}
