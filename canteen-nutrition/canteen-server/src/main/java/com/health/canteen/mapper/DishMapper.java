package com.health.canteen.mapper;

import com.health.canteen.entity.Dish;
import com.health.canteen.dto.DishPageQueryDTO;
import com.health.canteen.dto.RecommendDishQueryDTO;
import com.health.canteen.vo.RecommendDishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 条件分页查询菜品
     */
    List<Dish> getDishPage(DishPageQueryDTO dishQueryParam);

    /**
     * 根据ID查询菜品（含档口、分类与营养摘要）
     */
    Dish getDishById(Integer id);

    /**
     * 新增菜品
     */
    void addDish(Dish dish);

    /**
     * 修改菜品
     */
    void updateDish(Dish dish);

    /**
     * 修改菜品上下架状态
     */
    void updateDishStatus(@Param("id") Integer id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 菜品审核
     */
    void updateDishAudit(@Param("id") Integer id, @Param("auditStatus") Integer auditStatus,
                         @Param("auditRemark") String auditRemark, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 批量删除菜品
     */
    void deleteDish(List<Integer> ids);

    /**
     * 累加菜品销量
     */
    void increaseSales(@Param("id") Integer id, @Param("quantity") Integer quantity);

    /**
     * 扣减库存，返回受影响行数；库存不足或不限库存时为0
     */
    int minusStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    /**
     * 归还库存（订单取消）
     */
    void plusStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    /**
     * 今日已下单数量（不含已取消），用于每日限量校验
     */
    int countTodaySold(@Param("id") Integer id);

    /**
     * 查询可参与推荐的菜品（已上架、审核通过且有营养数据）
     */
    List<RecommendDishVO> getRecommendingDishes(RecommendDishQueryDTO recommendDishQuery);

    /**
     * 统计档口下的菜品数量
     */
    Integer countByStallId(Integer stallId);

}
