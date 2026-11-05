package com.example.flight.user.controller;

import com.example.flight.common.result.Result;
import com.example.flight.common.security.UserContext;
import com.example.flight.user.dto.LoginRequest;
import com.example.flight.user.dto.RegisterRequest;
import com.example.flight.user.dto.UserInfoVO;
import com.example.flight.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户模块", description = "注册 / 登录 / 退出（BCrypt 加密 + JWT + Redis Token 管理）")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册（BCrypt 加密入库，注册即登录签发 JWT）")
    @PostMapping("/register")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @Operation(summary = "用户登录（校验密码、签发 JWT 并存入 Redis）")
    @PostMapping("/login")
    public Result<UserInfoVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Operation(summary = "退出登录（删除 Redis Token，服务端立即失效）")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout(UserContext.getUserId());
        return Result.success();
    }
}
