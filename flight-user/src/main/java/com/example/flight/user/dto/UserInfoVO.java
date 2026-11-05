package com.example.flight.user.dto;

import lombok.Data;

/**
 * 用户信息视图对象（对外输出，绝不包含 password）
 */
@Data
public class UserInfoVO {

    private Long id;

    private String username;

    private String nickname;

    private String phone;

    private String role;

    /** 登录/注册成功后签发的 JWT */
    private String token;
}
