package com.health.canteen.exception;

import com.health.canteen.constant.MessageConstant;

/**
 * 账号被禁用
 */
public class AccountDisabledException extends BaseException {

    public AccountDisabledException() {
        super(MessageConstant.ACCOUNT_DISABLED);
    }

}
