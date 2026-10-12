package com.health.canteen.controller.common;

import com.health.canteen.dto.LoginDTO;
import com.health.canteen.vo.LoginVO;
import com.health.canteen.dto.RegisterDTO;
import com.health.canteen.result.Result;
import com.health.canteen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("commonLoginController")
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 登录：学生、商户、管理员统一入口
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginParam) {
        log.info("用户登录: {}", loginParam.getUsername());
        LoginVO loginResult = userService.login(loginParam);
        return Result.success(loginResult);
    }

    /**
     * 学生自助注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerParam) {
        log.info("学生注册: {}", registerParam.getUsername());
        userService.register(registerParam);
        return Result.success();
    }

    /**
     * 退出登录：删除Redis中的登录态，使当前JWT令牌立即失效
     */
    @PostMapping("/logout")
    public Result logout() {
        log.info("用户退出登录");
        userService.logout();
        return Result.success();
    }

}
