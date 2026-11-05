package com.example.flight.common.result;

import lombok.Getter;

/**
 * 统一错误码枚举（按讲义 2.4，后续每天往里加）
 */
@Getter
public enum ResultCode {

    PARAM_ERROR(10001, "参数错误"),

    USER_EXISTS(20001, "用户名已存在"),
    USER_OR_PASS_WRONG(20002, "用户名或密码错误"),
    USER_DISABLED(20003, "账号已被禁用"),

    TOKEN_INVALID(30001, "未登录或 Token 失效"),

    FLIGHT_NOT_FOUND(40001, "航班不存在"),
    NO_TICKET(50001, "票已售罄"),

    SYSTEM_ERROR(99999, "系统开小差了");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
