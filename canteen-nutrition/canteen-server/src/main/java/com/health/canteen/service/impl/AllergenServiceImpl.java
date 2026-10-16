package com.health.canteen.service.impl;

import com.health.canteen.entity.Allergen;
import com.health.canteen.entity.Dish;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.AllergenMapper;
import com.health.canteen.mapper.DishMapper;
import com.health.canteen.service.AllergenService;
import com.health.canteen.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AllergenServiceImpl implements AllergenService {

    @Autowired
    private AllergenMapper allergenMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    public List<Allergen> listAll() {
        return allergenMapper.listAll();
    }

    @Override
    public List<Allergen> listByDish(Integer dishId) {
        return allergenMapper.listByDishId(dishId);
    }

    @Override
    @Transactional
    public void setDishAllergens(Integer dishId, List<Integer> allergenIds) {
        Dish dish = dishMapper.getDishById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        CurrentUser.checkMerchantOwner(dish.getMerchantId());
        allergenMapper.deleteByDishId(dishId);
        if (allergenIds != null && !allergenIds.isEmpty()) {
            allergenMapper.batchInsert(dishId, allergenIds);
        }
        log.info("设置菜品过敏原, 菜品: {}, 数量: {}", dishId, allergenIds == null ? 0 : allergenIds.size());
    }
}
