package com.health.canteen.service;

import com.health.canteen.result.PageResult;
import com.health.canteen.entity.Review;
import com.health.canteen.dto.ReviewPageQueryDTO;

import java.util.List;

public interface ReviewService {

    PageResult<Review> getReviewPage(ReviewPageQueryDTO reviewQueryParam);

    void addReview(Review review);

    void updateStatus(Integer id, Integer status);

    void deleteReview(List<Integer> ids);

}
