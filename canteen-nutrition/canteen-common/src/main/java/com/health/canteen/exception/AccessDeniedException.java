package com.health.canteen.exception;

import com.health.canteen.constant.MessageConstant;

/**
 * 越权访问异常：角色不足或试图操作其他商户的数据
 */
public class AccessDeniedException extends BaseException {

    public AccessDeniedException() {
        super(MessageConstant.ACCESS_DENIED);
    }

    public AccessDeniedException(String msg) {
        super(msg);
    }

}
