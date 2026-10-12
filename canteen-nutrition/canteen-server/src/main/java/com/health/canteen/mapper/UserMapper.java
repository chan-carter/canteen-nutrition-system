package com.health.canteen.mapper;

import com.health.canteen.entity.User;
import com.health.canteen.dto.UserPageQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 条件分页查询用户
     */
    List<User> getUserPage(UserPageQueryDTO userQueryParam);

    /**
     * 根据ID查询用户（不含密码）
     */
    User getUserById(Integer id);

    /**
     * 根据账号查询用户（含密码，仅供登录校验使用）
     */
    User getByUsername(String username);

    /**
     * 新增用户
     */
    void addUser(User user);

    /**
     * 修改用户
     */
    void updateUser(User user);

    /**
     * 批量删除用户
     */
    void deleteUser(List<Integer> ids);

    /**
     * 修改密码
     */
    void updatePassword(@Param("id") Integer id, @Param("password") String password, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 修改账号状态
     */
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 统计指定商户下的账号数量，用于删除商户前的关联校验
     */
    Integer countByMerchantId(Integer merchantId);

}
