package com.example.flight.flight.controller;

import com.example.flight.common.result.Result;
import com.example.flight.flight.entity.Flight;
import com.example.flight.flight.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "航班查询模块", description = "按城市+日期搜索航班（Redis 缓存加速，需登录）")
@RestController
@RequestMapping("/api/flight")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @Operation(summary = "航班搜索（先查 Redis 缓存，未命中降级查 MySQL 并回填）")
    @GetMapping("/search")
    public Result<List<Flight>> search(@RequestParam String fromCity,
                                       @RequestParam String toCity,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                       @RequestParam(required = false) String seatType) {
        List<Flight> flights = flightService.searchFlights(fromCity, toCity, date, seatType);
        return Result.success(flights);
    }
}
