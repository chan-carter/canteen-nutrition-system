package com.health.canteen.exception;

/**
 * 业务异常根类：所有可预期的业务错误都继承它，由全局异常处理器统一转换为失败响应
 */
public abstract class BaseException extends RuntimeException {

    public BaseException(String msg) {
        super(msg);
    }

}
