package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish {
    private Integer id; //ID
    private Integer stallId; //所属档口ID
    private Integer categoryId; //菜品分类ID
    private String name; //菜品名称
    private BigDecimal price; //价格
    private String ingredients; //主要食材
    private String image; //菜品图片
    private String description; //菜品介绍
    private Integer status; //上架状态：1上架，0下架
    private Integer auditStatus; //审核状态：0待审核，1通过，2驳回
    private String auditRemark; //审核意见
    private Integer sales; //累计销量
    private Integer stock; //当前库存，null表示不限
    private Integer dailyLimit; //每日限量，null表示不限
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String stallName; //档口名称
    private Integer merchantId; //所属商户ID（通过档口关联）
    private String categoryName; //分类名称
    private BigDecimal calorie; //热量（kcal）
    private BigDecimal protein; //蛋白质（g）
    private BigDecimal fat; //脂肪（g）
    private BigDecimal carbohydrate; //碳水化合物（g）
    private Integer nutritionFlag; //是否已有营养数据：1有，0无
}
