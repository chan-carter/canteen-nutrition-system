package com.health.canteen.exception;

/**
 * 通用业务异常：用于参数校验不通过、数据不存在等可预期错误
 */
public class BusinessException extends BaseException {

    public BusinessException(String msg) {
        super(msg);
    }

}
