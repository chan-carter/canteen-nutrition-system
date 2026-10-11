package com.health.canteen.exception;

import com.health.canteen.constant.MessageConstant;

/**
 * 账号或密码错误：出于安全考虑不区分账号不存在与密码错误，统一返回同一提示
 */
public class LoginFailedException extends BaseException {

    public LoginFailedException() {
        super(MessageConstant.LOGIN_FAILED);
    }

}
