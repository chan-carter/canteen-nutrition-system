package com.health.canteen.controller.admin;

import com.health.canteen.annotation.Log;
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
@RestController("adminReviewController")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 屏蔽或恢复评价（仅管理员）
     */
    @PutMapping("/{id}/status")
    @RequireRole(3)
    @Log(module = "评价管理", operation = "屏蔽或恢复评价")
    public Result updateStatus(@PathVariable Integer id, @RequestBody Review review) {
        log.info("修改评价状态: {}, {}", id, review.getStatus());
        reviewService.updateStatus(id, review.getStatus());
        return Result.success();
    }

    /**
     * 删除异常评价（仅管理员）
     */
    @DeleteMapping("/{ids}")
    @RequireRole(3)
    @Log(module = "评价管理", operation = "删除评价")
    public Result deleteReview(@PathVariable List<Integer> ids) {
        log.info("删除评价: {}", ids);
        reviewService.deleteReview(ids);
        return Result.success();
    }

}
