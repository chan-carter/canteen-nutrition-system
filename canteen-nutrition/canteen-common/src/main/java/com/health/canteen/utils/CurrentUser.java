package com.health.canteen.utils;

import com.health.canteen.constant.MessageConstant;
import com.health.canteen.constant.RoleConstant;
import com.health.canteen.context.BaseContext;
import com.health.canteen.context.LoginUser;
import com.health.canteen.exception.AccessDeniedException;
import com.health.canteen.exception.BusinessException;

/**
 * 当前登录用户读取工具，角色与数据归属校验统一从这里取值
 */
public class CurrentUser {

    public static LoginUser get() {
        LoginUser loginUser = BaseContext.get();
        if (loginUser == null) {
            throw new BusinessException("获取登录信息失败，请重新登录");
        }
        return loginUser;
    }

    public static Integer id() {
        return get().getUserId();
    }

    public static Integer role() {
        return get().getRole();
    }

    public static boolean isStudent() {
        return Integer.valueOf(RoleConstant.ROLE_STUDENT).equals(role());
    }

    public static boolean isMerchant() {
        return Integer.valueOf(RoleConstant.ROLE_MERCHANT).equals(role());
    }

    public static boolean isAdmin() {
        return Integer.valueOf(RoleConstant.ROLE_ADMIN).equals(role());
    }

    /**
     * 缓存键中的可见性范围：商户账号按所属商户隔离，其余角色共用同一范围。
     * 缓存键必须带上它，否则商户B会直接命中商户A的菜品缓存而绕过归属校验。
     */
    public static Integer cacheScope() {
        return isMerchant() ? get().getMerchantId() : 0;
    }

    /**
     * 当前商户账号所绑定的商户ID，未绑定时视为越权访问
     */
    public static Integer merchantId() {
        Integer merchantId = get().getMerchantId();
        if (merchantId == null) {
            throw new AccessDeniedException(MessageConstant.MERCHANT_NOT_BOUND);
        }
        return merchantId;
    }

    /**
     * 校验目标商户是否为当前登录商户，管理员可跳过
     */
    public static void checkMerchantOwner(Integer merchantId) {
        if (isAdmin()) {
            return;
        }
        if (merchantId == null || !merchantId.equals(get().getMerchantId())) {
            throw new AccessDeniedException();
        }
    }

}
