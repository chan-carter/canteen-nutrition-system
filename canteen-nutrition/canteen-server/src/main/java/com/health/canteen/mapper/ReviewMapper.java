package com.health.canteen.mapper;

import com.health.canteen.entity.Review;
import com.health.canteen.dto.ReviewPageQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {

    /**
     * 条件分页查询评价
     */
    List<Review> getReviewPage(ReviewPageQueryDTO reviewQueryParam);

    /**
     * 根据ID查询评价
     */
    Review getReviewById(Integer id);

    /**
     * 新增评价
     */
    void addReview(Review review);

    /**
     * 屏蔽或恢复评价
     */
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 批量删除评价
     */
    void deleteReview(List<Integer> ids);

    /**
     * 判断同一订单中同一菜品是否已评价
     */
    Integer countDuplicate(@Param("userId") Integer userId, @Param("dishId") Integer dishId, @Param("orderId") Integer orderId);

}
