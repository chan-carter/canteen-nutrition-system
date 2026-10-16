package com.health.canteen.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收藏列表项（带菜品概要）
 */
@Data
public class FavoriteDishVO {
    private Integer dishId;
    private String dishName;
    private BigDecimal price;
    private String image;
    private BigDecimal calorie;
    private String stallName;
    private Integer status; //菜品上架状态
}
