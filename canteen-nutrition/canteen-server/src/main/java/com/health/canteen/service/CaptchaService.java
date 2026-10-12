package com.health.canteen.service;

import com.health.canteen.vo.CaptchaVO;

public interface CaptchaService {

    /**
     * 生成图形验证码，答案以 uuid 为键存入 Redis
     */
    CaptchaVO generate();

    /**
     * 校验验证码，失败抛出业务异常；校验通过后立即失效，不允许重复使用
     */
    void verify(String key, String code);

}
