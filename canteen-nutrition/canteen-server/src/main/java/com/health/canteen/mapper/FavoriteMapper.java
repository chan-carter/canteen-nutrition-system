package com.health.canteen.mapper;

import com.health.canteen.vo.FavoriteDishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    void add(@Param("userId") Integer userId, @Param("dishId") Integer dishId);

    void delete(@Param("userId") Integer userId, @Param("dishId") Integer dishId);

    List<FavoriteDishVO> listFavoriteDishes(@Param("userId") Integer userId);
}
