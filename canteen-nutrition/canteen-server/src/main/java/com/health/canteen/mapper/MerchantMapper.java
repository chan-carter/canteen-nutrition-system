package com.health.canteen.mapper;

import com.health.canteen.entity.Merchant;
import com.health.canteen.dto.MerchantPageQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MerchantMapper {

    /**
     * 条件分页查询商户
     */
    List<Merchant> getMerchantPage(MerchantPageQueryDTO merchantQueryParam);

    /**
     * 根据ID查询商户
     */
    Merchant getMerchantById(Integer id);

    /**
     * 新增商户
     */
    void addMerchant(Merchant merchant);

    /**
     * 修改商户
     */
    void updateMerchant(Merchant merchant);

    /**
     * 修改商户状态
     */
    void updateMerchantStatus(@Param("id") Integer id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 批量删除商户
     */
    void deleteMerchant(List<Integer> ids);

}
