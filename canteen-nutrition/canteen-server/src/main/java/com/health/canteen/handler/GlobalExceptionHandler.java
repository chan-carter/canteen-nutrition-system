package com.health.canteen.handler;

import com.health.canteen.constant.MessageConstant;
import com.health.canteen.exception.BaseException;
import com.health.canteen.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：包含越权、登录失败等，提示信息可直接返回给前端
     */
    @ExceptionHandler
    public Result handleBaseException(BaseException e) {
        log.warn("业务校验未通过: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler
    public Result handleException(Exception e) {
        log.error("程序出错啦~", e);
        return Result.error("系统异常, 请联系管理员~");
    }

    @ExceptionHandler
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("唯一约束冲突", e);
        String message = e.getMessage();
        int i = message == null ? -1 : message.indexOf("Duplicate entry");
        if (i < 0) {
            return Result.error("数据已存在");
        }
        String[] arr = message.substring(i).split(" ");
        return Result.error(arr.length > 2 ? arr[2].replaceAll("['\"]", "") + " 已存在" : "数据已存在");
    }

    @ExceptionHandler
    public Result handleValidationException(MethodArgumentNotValidException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return Result.error("参数校验失败，请检查必填项");
    }

    @ExceptionHandler
    public Result handleNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.error("请求参数格式不正确，请检查JSON内容与编码");
    }

    @ExceptionHandler
    public Result handleMissingParamException(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return Result.error("缺少必要参数：" + e.getParameterName());
    }

    /**
     * Redis未启动时给出明确提示，避免前端只看到一句系统异常
     */
    @ExceptionHandler
    public Result handleRedisException(RedisConnectionFailureException e) {
        log.error("Redis连接失败", e);
        return Result.error(MessageConstant.REDIS_UNAVAILABLE);
    }

}
