package com.health.canteen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 营养分析请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionAnalyzeDTO {
    private String dishName; //可不传，默认取库中菜品名称
    private List<String> ingredients; //可不传，默认按顿号切分库中食材
    private String servingSize; //参考份量
}
