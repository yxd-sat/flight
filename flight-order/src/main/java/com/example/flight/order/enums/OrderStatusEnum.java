package com.example.flight.order.enums;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    PENDING_PAY(0, "待支付"),
    PAID(1, "已支付"),
    TICKET_ISSUED(2, "已出票"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isValidTransition(int currentStatus, int targetStatus) {
        if (currentStatus == PENDING_PAY.code && (targetStatus == PAID.code || targetStatus == CANCELLED.code)) {
            return true;
        }
        if (currentStatus == PAID.code && targetStatus == TICKET_ISSUED.code) {
            return true;
        }
        return false;
    }

    public static OrderStatusEnum getByCode(int code) {
        for (OrderStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知订单状态: " + code);
    }
}
