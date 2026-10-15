package com.health.canteen.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    List<Map<String, Object>> getDishCategoryData();

    /**
     * 菜品销量Top10，merchantId 为空表示管理员看全局
     */
    List<Map<String, Object>> getDishSalesData(@Param("merchantId") Integer merchantId);

    List<Map<String, Object>> getHealthGoalData();

    List<Map<String, Object>> getOrderData();

    List<Map<String, Object>> getReviewData();

    Map<String, Object> getOverviewData();

}
