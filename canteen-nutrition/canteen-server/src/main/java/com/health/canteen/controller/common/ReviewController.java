package com.health.canteen.controller.common;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.result.PageResult;
import com.health.canteen.result.Result;
import com.health.canteen.entity.Review;
import com.health.canteen.dto.ReviewPageQueryDTO;
import com.health.canteen.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RequestMapping("/reviews")
@RestController("commonReviewController")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 分页查询评价
     */
    @GetMapping
    public Result getReviewPage(ReviewPageQueryDTO reviewQueryParam) {
        log.info("分页查询评价: {}", reviewQueryParam);
        PageResult<Review> pageResult = reviewService.getReviewPage(reviewQueryParam);
        return Result.success(pageResult);
    }

}
