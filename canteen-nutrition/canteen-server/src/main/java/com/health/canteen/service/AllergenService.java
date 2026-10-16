package com.health.canteen.service;

import com.health.canteen.entity.Allergen;

import java.util.List;

public interface AllergenService {

    List<Allergen> listAll();

    List<Allergen> listByDish(Integer dishId);

    void setDishAllergens(Integer dishId, List<Integer> allergenIds);
}
