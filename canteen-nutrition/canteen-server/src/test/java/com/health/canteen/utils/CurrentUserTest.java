package com.health.canteen.utils;

import com.health.canteen.context.BaseContext;
import com.health.canteen.context.LoginUser;
import com.health.canteen.exception.AccessDeniedException;
import com.health.canteen.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 全站越权校验都走这个工具类，令牌里没有身份就直接失败，绝不放行。
 */
@DisplayName("登录上下文与越权校验")
class CurrentUserTest {

    @AfterEach
    void clear() {
        BaseContext.remove();
    }

    @Test
    @DisplayName("无上下文时取用户信息直接报错")
    void failsWithoutContext() {
        assertThatThrownBy(CurrentUser::get).isInstanceOf(BusinessException.class);
        assertThatThrownBy(CurrentUser::id).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("角色判定按令牌里的 role")
    void resolvesRoles() {
        BaseContext.set(new LoginUser(4, "canteen01", 2, 1));
        assertThat(CurrentUser.isMerchant()).isTrue();
        assertThat(CurrentUser.isStudent()).isFalse();
        assertThat(CurrentUser.isAdmin()).isFalse();
        assertThat(CurrentUser.merchantId()).isEqualTo(1);
    }

    @Test
    @DisplayName("商户账号未绑定商户时不能访问商户数据")
    void merchantWithoutBindingIsDenied() {
        BaseContext.set(new LoginUser(4, "broken", 2, null));

        assertThatThrownBy(CurrentUser::merchantId).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("商户只能碰自己的数据，管理员可跳过；目标为空也视为越权")
    void checksMerchantOwner() {
        BaseContext.set(new LoginUser(4, "canteen01", 2, 1));
        CurrentUser.checkMerchantOwner(1);
        assertThatThrownBy(() -> CurrentUser.checkMerchantOwner(2)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> CurrentUser.checkMerchantOwner(null)).isInstanceOf(AccessDeniedException.class);

        BaseContext.set(new LoginUser(6, "admin", 3, null));
        CurrentUser.checkMerchantOwner(2);
    }

    @Test
    @DisplayName("缓存键的可见性范围：商户按商户号隔离，其余共用 0")
    void cacheScopeIsolatesMerchants() {
        BaseContext.set(new LoginUser(4, "canteen01", 2, 7));
        assertThat(CurrentUser.cacheScope()).isEqualTo(7);

        BaseContext.set(new LoginUser(6, "admin", 3, null));
        assertThat(CurrentUser.cacheScope()).isZero();
    }

}
