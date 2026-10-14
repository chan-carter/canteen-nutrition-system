package com.health.canteen.controller.student;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.dto.RecommendFeedbackDTO;
import com.health.canteen.result.PageResult;
import com.health.canteen.vo.RecommendDishVO;
import com.health.canteen.dto.RecommendationPageQueryDTO;
import com.health.canteen.result.Result;
import com.health.canteen.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequestMapping("/recommendations")
@RestController("studentRecommendationController")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 获取个性化推荐（仅学生）
     */
    @GetMapping
    @RequireRole(1)
    public Result getRecommendations(RecommendationPageQueryDTO queryParam) {
        log.info("获取个性化推荐: {}", queryParam);
        PageResult<RecommendDishVO> pageResult = recommendationService.getRecommendations(queryParam);
        return Result.success(pageResult);
    }

    /**
     * 按当前学生的资料重新生成推荐并保存（仅学生）
     */
    @PostMapping("/refresh")
    @RequireRole(1)
    public Result refresh() {
        log.info("刷新个性化推荐");
        int count = recommendationService.refresh();
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    /**
     * 对推荐结果点赞/踩（仅学生）
     */
    @PostMapping("/feedback")
    @RequireRole(1)
    public Result feedback(@RequestBody RecommendFeedbackDTO dto) {
        log.info("推荐反馈: {}", dto);
        recommendationService.feedback(dto.getDishId(), dto.getAction(), dto.getReason());
        return Result.success();
    }

}
