package com.health.canteen.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.DishMapper;
import com.health.canteen.mapper.OrderItemMapper;
import com.health.canteen.mapper.OrdersMapper;
import com.health.canteen.mapper.ReviewMapper;
import com.health.canteen.context.*;
import com.health.canteen.dto.*;
import com.health.canteen.entity.*;
import com.health.canteen.result.*;
import com.health.canteen.vo.*;
import com.health.canteen.service.ReviewService;
import com.health.canteen.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final int ORDER_FINISHED = 3;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public PageResult<Review> getReviewPage(ReviewPageQueryDTO reviewQueryParam) {
        if (CurrentUser.isStudent()) {
            //学生端只展示正常状态的评价
            reviewQueryParam.setStatus(1);
            reviewQueryParam.setUserId(null);
        } else if (CurrentUser.isMerchant()) {
            //商户可查看自己菜品的评价，但不限制具体菜品，便于整体分析偏好
            reviewQueryParam.setStatus(null);
        }
        PageHelper.startPage(reviewQueryParam.getPage(), reviewQueryParam.getPageSize());
        List<Review> reviews = reviewMapper.getReviewPage(reviewQueryParam);
        Page<Review> page = (Page<Review>) reviews;
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    public void addReview(Review review) {
        if (review.getDishId() == null) {
            throw new BusinessException("请选择要评价的菜品");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new BusinessException("评分需要在1到5之间");
        }
        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            throw new BusinessException("评价内容不能为空");
        }
        if (review.getContent().length() > 500) {
            throw new BusinessException("评价内容不能超过500字");
        }
        Dish dish = dishMapper.getDishById(review.getDishId());
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }

        Integer userId = CurrentUser.id();
        if (review.getOrderId() == null) {
            throw new BusinessException("请选择关联的订单");
        }
        Orders orders = ordersMapper.getOrdersById(review.getOrderId());
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(orders.getUserId())) {
            throw new BusinessException("只能评价本人订单中的菜品");
        }
        if (!Integer.valueOf(ORDER_FINISHED).equals(orders.getStatus())) {
            throw new BusinessException("订单完成后才可以评价");
        }
        boolean ordered = false;
        for (OrderItem item : orderItemMapper.getByOrderId(orders.getId())) {
            if (item.getDishId().equals(review.getDishId())) {
                ordered = true;
                break;
            }
        }
        if (!ordered) {
            throw new BusinessException("该菜品不在所选订单中，无法评价");
        }
        if (reviewMapper.countDuplicate(userId, review.getDishId(), review.getOrderId()) > 0) {
            throw new BusinessException("该订单中的这道菜品已评价过");
        }

        review.setUserId(userId);
        review.setStatus(1);
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.addReview(review);
        log.info("评价提交成功, 用户: {}, 菜品: {}, 评分: {}", userId, review.getDishId(), review.getRating());
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态取值不正确");
        }
        if (reviewMapper.getReviewById(id) == null) {
            throw new BusinessException("评价不存在");
        }
        reviewMapper.updateStatus(id, status);
    }

    @Override
    public void deleteReview(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的评价");
        }
        reviewMapper.deleteReview(ids);
    }

}
