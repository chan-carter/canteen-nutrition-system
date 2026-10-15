package com.health.canteen.controller.student;

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
@RestController("studentReviewController")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 学生提交评价
     */
    @PostMapping
    @RequireRole(1)
    public Result addReview(@RequestBody Review review) {
        log.info("提交评价: {}", review);
        reviewService.addReview(review);
        return Result.success();
    }

}
