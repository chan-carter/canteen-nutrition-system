package com.health.canteen.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 过敏原字典
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Allergen {
    private Integer id;
    private String code;
    private String name;
}
