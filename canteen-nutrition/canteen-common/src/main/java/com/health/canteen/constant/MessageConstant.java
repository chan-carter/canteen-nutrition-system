package com.health.canteen.constant;

/**
 * 提示信息常量：鉴权与登录相关的提示统一在此定义，避免散落在各处
 */
public class MessageConstant {

    public static final String NOT_LOGIN = "用户未登录或登录已失效，请重新登录";

    public static final String LOGIN_EXPIRED = "登录状态已在其他设备更新或已注销，请重新登录";

    public static final String NO_PERMISSION = "当前角色无权访问该功能";

    public static final String LOGIN_FAILED = "账号或密码错误";

    public static final String ACCOUNT_DISABLED = "账号已被禁用，请联系管理员";

    public static final String ACCESS_DENIED = "只能操作本商户的数据";

    public static final String MERCHANT_NOT_BOUND = "当前账号未绑定商户，无法操作商户数据";

    public static final String SYSTEM_ERROR = "系统异常, 请联系管理员~";

    public static final String REDIS_UNAVAILABLE = "缓存服务不可用，请确认Redis已启动";

}
