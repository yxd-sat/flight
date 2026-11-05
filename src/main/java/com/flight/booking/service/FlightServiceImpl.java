package com.flight.booking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flight.booking.entity.Flight;
import com.flight.booking.entity.FlightSeat;
import com.flight.booking.mapper.FlightMapper;
import com.flight.booking.mapper.FlightSeatMapper;
import com.flight.booking.service.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightMapper flightMapper;
    private final FlightSeatMapper flightSeatMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<Flight> searchFlights(String departCity, String arriveCity, LocalDate date, String seatType) {
        String cacheKey = String.format("FLIGHT:SEARCH:%s:%s:%s", departCity, arriveCity, date);

        try {
            String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cachedJson)) {
                log.info("[Redis Cache Hit] 命中航班搜索缓存: key={}", cacheKey);
                return objectMapper.readValue(cachedJson, new TypeReference<List<Flight>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis 缓存查询异常，自动降级查 MySQL", e);
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        LambdaQueryWrapper<Flight> wrapper = new LambdaQueryWrapper<Flight>()
                .eq(Flight::getDepartCity, departCity)
                .eq(Flight::getArriveCity, arriveCity)
                .between(Flight::getDepartTime, startOfDay, endOfDay)
                .eq(Flight::getStatus, 1);

        List<Flight> flights = flightMapper.selectList(wrapper);

        for (Flight f : flights) {
            List<FlightSeat> seats = flightSeatMapper.selectList(
                    new LambdaQueryWrapper<FlightSeat>().eq(FlightSeat::getFlightId, f.getId())
            );
            f.setSeats(seats);
        }

        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(flights),
                    10,
                    TimeUnit.MINUTES
            );
            log.info("[Redis Cache Write] 写入航班缓存成功: key={}", cacheKey);
        } catch (Exception e) {
            log.error("写入 Redis 缓存失败", e);
        }

        return flights;
    }
}
