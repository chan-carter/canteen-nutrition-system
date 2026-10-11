package com.health.canteen.interceptor;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.constant.MessageConstant;
import com.health.canteen.constant.RedisConstant;
import com.health.canteen.context.BaseContext;
import com.health.canteen.context.LoginUser;
import com.health.canteen.result.Result;
import com.health.canteen.utils.JsonUtils;
import com.health.canteen.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 登录校验与角色权限校验拦截器。
 * 令牌除了自身签名与有效期校验外，还必须与Redis中保存的登录态一致，
 * 这样退出登录或重新登录后，旧令牌会立即失效。
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String token = request.getHeader("token");
        LoginUser loginUser = jwtUtil.parseToken(token);
        if (loginUser == null || loginUser.getUserId() == null) {
            log.warn("未登录或令牌失效, 请求路径: {}", request.getRequestURI());
            writeToResponse(response, 401, Result.error(MessageConstant.NOT_LOGIN));
            return false;
        }
        if (!isCurrentToken(loginUser.getUserId(), token)) {
            log.warn("令牌与Redis登录态不一致, 用户: {}, 请求路径: {}", loginUser.getUsername(), request.getRequestURI());
            writeToResponse(response, 401, Result.error(MessageConstant.LOGIN_EXPIRED));
            return false;
        }
        BaseContext.set(loginUser);

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && !allowed(requireRole.value(), loginUser.getRole())) {
            log.warn("越权访问被拦截, 用户角色: {}, 请求路径: {}", loginUser.getRole(), request.getRequestURI());
            writeToResponse(response, 403, Result.error(MessageConstant.NO_PERMISSION));
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.remove();
    }

    private boolean isCurrentToken(Integer userId, String token) {
        String cached = stringRedisTemplate.opsForValue().get(RedisConstant.LOGIN_TOKEN_KEY + userId);
        return token.equals(cached);
    }

    private boolean allowed(int[] roles, Integer role) {
        if (role == null) {
            return false;
        }
        for (int item : roles) {
            if (item == role) {
                return true;
            }
        }
        return false;
    }

    private void writeToResponse(HttpServletResponse response, int status, Result result) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JsonUtils.toJson(result));
    }

}
