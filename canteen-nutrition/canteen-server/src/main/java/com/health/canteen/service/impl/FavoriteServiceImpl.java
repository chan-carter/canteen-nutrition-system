package com.health.canteen.service.impl;

import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.DishMapper;
import com.health.canteen.mapper.FavoriteMapper;
import com.health.canteen.service.FavoriteService;
import com.health.canteen.utils.CurrentUser;
import com.health.canteen.vo.FavoriteDishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    public List<FavoriteDishVO> list() {
        return favoriteMapper.listFavoriteDishes(CurrentUser.id());
    }

    @Override
    public void add(Integer dishId) {
        if (dishId == null || dishMapper.getDishById(dishId) == null) {
            throw new BusinessException("菜品不存在");
        }
        favoriteMapper.add(CurrentUser.id(), dishId);
    }

    @Override
    public void cancel(Integer dishId) {
        favoriteMapper.delete(CurrentUser.id(), dishId);
    }
}
