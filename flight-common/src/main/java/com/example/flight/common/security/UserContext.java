package com.example.flight.common.security;

/**
 * 当前登录用户上下文（ThreadLocal）。
 * JwtInterceptor 校验通过后写入，业务代码任意位置可通过 UserContext.getUserId() 获取当前用户，
 * 避免层层透传 userId 参数；请求结束时由拦截器 afterCompletion 清理，防止线程池串号。
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setCurrent(Long userId, String username) {
        USER_ID.set(userId);
        USERNAME.set(username);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}
