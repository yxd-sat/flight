package com.example.flight.common.security;

import com.example.flight.common.exception.BusinessException;
import com.example.flight.common.result.ResultCode;
import com.example.flight.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器：
 * 1. 校验 Authorization: Bearer <token> 签名与有效期；
 * 2. 与 Redis 中 USER_TOKEN:{userId} 比对，支持服务端强制下线（单设备登录）；
 * 3. 通过后写入 UserContext（ThreadLocal），请求结束清理。
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    public static final String TOKEN_REDIS_PREFIX = "USER_TOKEN:";

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "请先登录（缺少 Authorization: Bearer 请求头）");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "登录凭证已过期或无效，请重新登录");
        }

        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);

        String redisToken = stringRedisTemplate.opsForValue().get(TOKEN_REDIS_PREFIX + userId);
        if (!token.equals(redisToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "账号已在其他终端登录或已登出");
        }

        // 写入上下文，供业务层直接获取当前用户
        UserContext.setCurrent(userId, username);
        // 兼容 @RequestAttribute 方式获取
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUsername", username);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 必须清理 ThreadLocal，防止 Tomcat 线程复用导致用户串号
        UserContext.clear();
    }
}
