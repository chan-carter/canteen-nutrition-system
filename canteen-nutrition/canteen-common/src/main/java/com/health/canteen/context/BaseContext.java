package com.health.canteen.context;

/**
 * 基于ThreadLocal的当前登录用户上下文：登录拦截器写入，业务层读取，请求结束后必须清理
 */
public class BaseContext {

    private static final ThreadLocal<LoginUser> CONTEXT = new ThreadLocal<>();

    public static void set(LoginUser loginUser) {
        CONTEXT.set(loginUser);
    }

    public static LoginUser get() {
        return CONTEXT.get();
    }

    public static Integer getUserId() {
        LoginUser loginUser = CONTEXT.get();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static Integer getRole() {
        LoginUser loginUser = CONTEXT.get();
        return loginUser == null ? null : loginUser.getRole();
    }

    public static Integer getMerchantId() {
        LoginUser loginUser = CONTEXT.get();
        return loginUser == null ? null : loginUser.getMerchantId();
    }

    public static void remove() {
        CONTEXT.remove();
    }

}
