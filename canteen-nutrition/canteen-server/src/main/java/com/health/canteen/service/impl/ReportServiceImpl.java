package com.health.canteen.service.impl;

import com.health.canteen.mapper.AiCallLogMapper;
import com.health.canteen.mapper.RecommendationMapper;
import com.health.canteen.mapper.ReportMapper;
import com.health.canteen.service.ReportService;
import com.health.canteen.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import com.health.canteen.constant.RedisConstant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private AiCallLogMapper aiCallLogMapper;

    @Cacheable(cacheNames = RedisConstant.REPORT_CACHE, key = "'dishCategory'")
    @Override
    public List<Map<String, Object>> getDishCategoryData() {
        return reportMapper.getDishCategoryData();
    }

    @Override
    public List<Map<String, Object>> getDishSalesData() {
        //商户只能看本店销量，管理员不受限；结果按调用者不同，故不加缓存
        Integer merchantId = CurrentUser.isMerchant() ? CurrentUser.merchantId() : null;
        return reportMapper.getDishSalesData(merchantId);
    }

    @Cacheable(cacheNames = RedisConstant.REPORT_CACHE, key = "'healthGoal'")
    @Override
    public List<Map<String, Object>> getHealthGoalData() {
        return reportMapper.getHealthGoalData();
    }

    @Cacheable(cacheNames = RedisConstant.REPORT_CACHE, key = "'order'")
    @Override
    public List<Map<String, Object>> getOrderData() {
        return reportMapper.getOrderData();
    }

    @Cacheable(cacheNames = RedisConstant.REPORT_CACHE, key = "'review'")
    @Override
    public List<Map<String, Object>> getReviewData() {
        return reportMapper.getReviewData();
    }

    @Cacheable(cacheNames = RedisConstant.REPORT_CACHE, key = "'recommendHot'")
    @Override
    public List<Map<String, Object>> getRecommendHotData() {
        return recommendationMapper.getRecommendHotData();
    }

    @Cacheable(cacheNames = RedisConstant.REPORT_CACHE, key = "'aiCall'")
    @Override
    public List<Map<String, Object>> getAiCallData() {
        return aiCallLogMapper.getCallCountData();
    }

    @Cacheable(cacheNames = RedisConstant.REPORT_CACHE, key = "'overview'")
    @Override
    public Map<String, Object> getOverviewData() {
        return reportMapper.getOverviewData();
    }

}
