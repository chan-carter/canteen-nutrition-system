package com.health.canteen.dto;

import lombok.Data;

import java.util.List;

/**
 * 设置某道菜的过敏原（全量覆盖）
 */
@Data
public class DishAllergenDTO {
    private List<Integer> allergenIds;
}
