package com.health.canteen.service;

import com.health.canteen.vo.FavoriteDishVO;

import java.util.List;

public interface FavoriteService {

    List<FavoriteDishVO> list();

    void add(Integer dishId);

    void cancel(Integer dishId);
}
