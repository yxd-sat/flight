package com.example.flight.user.service;

import com.example.flight.user.dto.LoginRequest;
import com.example.flight.user.dto.RegisterRequest;
import com.example.flight.user.dto.UserInfoVO;

public interface UserService {

    /**
     * 用户注册：用户名唯一校验 + BCrypt 加密入库，注册即登录返回 Token
     */
    UserInfoVO register(RegisterRequest request);

    /**
     * 用户登录：BCrypt 密码比对，签发 JWT 并写入 Redis
     */
    UserInfoVO login(LoginRequest request);

    /**
     * 退出登录：删除 Redis 中的 Token（服务端立即失效）
     */
    void logout(Long userId);
}
