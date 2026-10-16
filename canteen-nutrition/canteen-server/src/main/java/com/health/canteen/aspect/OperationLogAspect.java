package com.health.canteen.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.canteen.annotation.Log;
import com.health.canteen.context.BaseContext;
import com.health.canteen.context.LoginUser;
import com.health.canteen.entity.OperationLog;
import com.health.canteen.mapper.OperationLogMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 拦截 @Log 标注的写接口，记录操作人、入参、耗时与结果到 operation_log。
 * 日志写失败不影响主业务（内部吞异常）。
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(logAnno)")
    public Object around(ProceedingJoinPoint point, Log logAnno) throws Throwable {
        long start = System.currentTimeMillis();
        String errorMsg = null;
        boolean success = true;
        try {
            return point.proceed();
        } catch (Throwable t) {
            success = false;
            errorMsg = t.getMessage();
            throw t;
        } finally {
            try {
                record(point, logAnno, success, errorMsg, (int) (System.currentTimeMillis() - start));
            } catch (Exception e) {
                log.warn("记录操作日志失败: {}", e.getMessage());
            }
        }
    }

    private void record(ProceedingJoinPoint point, Log logAnno, boolean success, String errorMsg, int cost) {
        OperationLog entity = new OperationLog();
        LoginUser user = BaseContext.get();
        if (user != null) {
            entity.setUserId(user.getUserId());
            entity.setUsername(user.getUsername());
        }
        entity.setModule(logAnno.module());
        entity.setOperation(logAnno.operation());
        MethodSignature sig = (MethodSignature) point.getSignature();
        entity.setMethod(sig.getDeclaringType().getSimpleName() + "." + sig.getName());
        entity.setParams(truncate(toJson(point.getArgs()), 900));
        entity.setIp(clientIp());
        entity.setCostMs(cost);
        entity.setSuccess(success ? 1 : 0);
        entity.setErrorMsg(truncate(errorMsg, 480));
        entity.setCreateTime(LocalDateTime.now());
        operationLogMapper.add(entity);
    }

    /**
     * 文件与请求响应对象不可序列化，也不适合落库，直接跳过
     */
    private String toJson(Object[] args) {
        List<Object> plain = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof ServletRequest || arg instanceof ServletResponse || arg instanceof MultipartFile) {
                continue;
            }
            plain.add(arg);
        }
        try {
            return plain.isEmpty() ? "-" : mask(objectMapper.writeValueAsString(plain));
        } catch (Exception e) {
            return "-";
        }
    }

    /**
     * 口令一类字段不能进日志表，序列化后统一打码
     */
    private String mask(String json) {
        if (json == null) {
            return null;
        }
        return json.replaceAll("(?i)\"(password|newPassword|oldPassword|apiKey)\"\\s*:\\s*(\"[^\"]*\"|null)",
                "\"$1\":\"***\"");
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    private String clientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty()) {
                int comma = ip.indexOf(',');
                return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
