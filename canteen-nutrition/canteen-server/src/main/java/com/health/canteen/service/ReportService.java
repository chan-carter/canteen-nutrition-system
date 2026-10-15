package com.health.canteen.service;

import java.util.List;
import java.util.Map;

public interface ReportService {

    List<Map<String, Object>> getDishCategoryData();

    List<Map<String, Object>> getDishSalesData();

    List<Map<String, Object>> getHealthGoalData();

    List<Map<String, Object>> getOrderData();

    List<Map<String, Object>> getReviewData();

    List<Map<String, Object>> getRecommendHotData();

    List<Map<String, Object>> getAiCallData();

    Map<String, Object> getOverviewData();

}
