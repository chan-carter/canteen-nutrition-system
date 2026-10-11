package com.health.canteen.constant;

import java.time.Duration;

/**
 * Redis键与缓存名常量
 */
public class RedisConstant {

    /**
     * 登录态：一个账号只保留最新一次登录签发的令牌，注销或重新登录后旧令牌立即失效
     */
    public static final String LOGIN_TOKEN_KEY = "canteen:login:token:";

    /**
     * AI营养分析原始结果，避免同一道菜的重复模型调用产生额外费用
     */
    public static final String AI_ANALYZE_KEY = "canteen:ai:analyze:";

    /**
     * 图形验证码答案：键为服务端下发的uuid，校验通过后立即删除，保证一次一用
     */
    public static final String CAPTCHA_KEY = "canteen:captcha:";

    public static final Duration CAPTCHA_TTL = Duration.ofMinutes(2);

    public static final String CATEGORY_CACHE = "categoryCache";

    public static final String DISH_CACHE = "dishCache";

    public static final String NUTRITION_CACHE = "nutritionCache";

    public static final String RECOMMEND_CACHE = "recommendCache";

    public static final String REPORT_CACHE = "reportCache";

    /**
     * SpringCache默认缓存名前缀，用于按模式批量清理
     */
    public static final String CACHE_KEY_PATTERN = "*Cache::*";

    public static final Duration AI_ANALYZE_TTL = Duration.ofHours(12);

}
