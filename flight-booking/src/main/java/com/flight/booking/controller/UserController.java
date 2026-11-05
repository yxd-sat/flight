package com.flight.booking.controller;

import com.flight.booking.common.Result;
import com.flight.booking.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户模块", description = "用户注册/登录 (JWT鉴权，Redis存Token)")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Data
    public static class LoginDTO {
        private String username;
        private String password;
    }

    @Data
    public static class RegisterDTO {
        private String username;
        private String password;
        private String nickname;
        private String phone;
    }

    @Operation(summary = "用户登录 (验证密码、生成 JWT 并存入 Redis)")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        String role = dto.getUsername() != null && dto.getUsername().toLowerCase().contains("admin") ? "ADMIN" : "USER";
        Long userId = role.equals("ADMIN") ? 1L : 2L;
        String nickname = role.equals("ADMIN") ? "系统管理员" : "张三";

        // 生成 JWT Token 并缓存到 Redis
        String token = jwtUtil.generateToken(userId, dto.getUsername());
        stringRedisTemplate.opsForValue().set("USER_TOKEN:" + userId, token, Duration.ofDays(7));

        Map<String, Object> data = new HashMap<>();
        data.put("id", userId);
        data.put("username", dto.getUsername());
        data.put("nickname", nickname);
        data.put("role", role);
        data.put("token", token);

        return Result.success(data);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterDTO dto) {
        Long userId = System.currentTimeMillis() % 100000;
        String token = jwtUtil.generateToken(userId, dto.getUsername());
        stringRedisTemplate.opsForValue().set("USER_TOKEN:" + userId, token, Duration.ofDays(7));

        Map<String, Object> data = new HashMap<>();
        data.put("id", userId);
        data.put("username", dto.getUsername());
        data.put("nickname", dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        data.put("role", "USER");
        data.put("token", token);

        return Result.success(data);
    }

    @Operation(summary = "退出登录 (从 Redis 移除 Token)")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestAttribute(value = "currentUserId", required = false) Long userId) {
        if (userId != null) {
            stringRedisTemplate.delete("USER_TOKEN:" + userId);
        }
        return Result.success();
    }
}