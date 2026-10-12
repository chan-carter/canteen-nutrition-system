package com.health.canteen.controller.common;

import com.health.canteen.result.Result;
import com.health.canteen.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("commonCaptchaController")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 获取图形验证码：无需登录，答案存Redis，前端提交时带回key
     */
    @GetMapping("/captcha")
    public Result getCaptcha() {
        log.info("获取图形验证码");
        return Result.success(captchaService.generate());
    }

}
