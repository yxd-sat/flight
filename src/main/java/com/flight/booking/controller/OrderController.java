package com.flight.booking.controller;

import com.flight.booking.common.Result;
import com.flight.booking.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Data
    public static class CreateOrderDTO {
        private Long flightId;
        private String seatType;
        private String passengerName;
        private String passengerIdCard;
    }

    @PostMapping("/create")
    public Result<String> createOrder(@RequestAttribute(value = "currentUserId", required = false) Long userId,
                                      @RequestBody CreateOrderDTO dto) {
        Long uid = (userId != null) ? userId : 2L;
        String orderNo = orderService.createOrder(uid, dto.getFlightId(), dto.getSeatType(), dto.getPassengerName(), dto.getPassengerIdCard());
        return Result.success(orderNo);
    }

    @PostMapping("/cancel")
    public Result<Void> cancelOrder(@RequestParam String orderNo, @RequestParam(defaultValue = "用户主动取消") String reason) {
        orderService.cancelOrder(orderNo, reason);
        return Result.success();
    }
}