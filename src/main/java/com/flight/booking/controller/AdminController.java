package com.flight.booking.controller;

import com.flight.booking.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员后台", description = "管理员航班调度与压测库存重置")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StringRedisTemplate stringRedisTemplate;

    @Operation(summary = "重置 CA1234 经济舱秒杀库存为 5 张 (供 200 并发压测)")
    @PostMapping("/stock/reset")
    public Result<String> resetStock(@RequestParam(defaultValue = "1001") Long flightId,
                                     @RequestParam(defaultValue = "Y") String seatType,
                                     @RequestParam(defaultValue = "5") Integer count) {
        String stockKey = String.format("FLIGHT:STOCK:%d:%s", flightId, seatType);
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(count));
        return Result.success("已将航班 " + flightId + " 舱位 " + seatType + " 的库存重置为 " + count + " 张！");
    }
}