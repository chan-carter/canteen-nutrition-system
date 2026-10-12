package com.health.canteen.mapper;

import com.health.canteen.entity.Stall;
import com.health.canteen.dto.StallPageQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StallMapper {

    /**
     * 条件分页查询档口
     */
    List<Stall> getStallPage(StallPageQueryDTO stallQueryParam);

    /**
     * 根据ID查询档口
     */
    Stall getStallById(Integer id);

    /**
     * 新增档口
     */
    void addStall(Stall stall);

    /**
     * 修改档口
     */
    void updateStall(Stall stall);

    /**
     * 修改档口状态
     */
    void updateStallStatus(@Param("id") Integer id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 批量删除档口
     */
    void deleteStall(List<Integer> ids);

    /**
     * 统计商户下的档口数量
     */
    Integer countByMerchantId(Integer merchantId);

    /**
     * 统计档口下的菜品数量
     */
    Integer countDishByStallId(Integer stallId);

}
