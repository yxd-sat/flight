package com.example.flight.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.flight.common.exception.BusinessException;
import com.example.flight.common.result.ResultCode;
import com.example.flight.common.security.JwtInterceptor;
import com.example.flight.common.util.JwtUtil;
import com.example.flight.user.converter.UserConverter;
import com.example.flight.user.dto.LoginRequest;
import com.example.flight.user.dto.RegisterRequest;
import com.example.flight.user.dto.UserInfoVO;
import com.example.flight.user.entity.User;
import com.example.flight.user.mapper.UserMapper;
import com.example.flight.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    /** BCrypt 单向加密：同一密码每次盐值不同，无法反推明文 */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserInfoVO register(RegisterRequest request) {
        // 1. 用户名唯一校验（数据库另有 uk_username 兜底）
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }

        // 2. 组装用户并加密入库
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        log.info("用户注册成功: id={}, username={}", user.getId(), user.getUsername());

        // 3. 注册即登录，直接签发 Token
        String token = issueToken(user);
        return UserConverter.toVO(user, token);
    }

    @Override
    public UserInfoVO login(LoginRequest request) {
        // 1. 按用户名查询（用户不存在与密码错误返回同一提示，防止用户名枚举）
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_OR_PASS_WRONG);
        }

        // 2. 状态校验
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 3. 签发 JWT 并存 Redis（TTL 与 JWT 有效期对齐，支持强制下线）
        String token = issueToken(user);
        log.info("用户登录成功: id={}, username={}", user.getId(), user.getUsername());
        return UserConverter.toVO(user, token);
    }

    @Override
    public void logout(Long userId) {
        if (userId != null) {
            stringRedisTemplate.delete(JwtInterceptor.TOKEN_REDIS_PREFIX + userId);
            log.info("用户退出登录，Redis Token 已删除: userId={}", userId);
        }
    }

    /**
     * 签发 JWT 并写入 Redis：key = USER_TOKEN:{userId}
     */
    private String issueToken(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        stringRedisTemplate.opsForValue().set(
                JwtInterceptor.TOKEN_REDIS_PREFIX + user.getId(),
                token,
                Duration.ofMillis(jwtUtil.getExpirationMillis()));
        return token;
    }
}
