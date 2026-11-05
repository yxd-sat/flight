package com.example.flight.flight.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.flight.flight.entity.Flight;
import com.example.flight.flight.entity.FlightSeat;
import com.example.flight.flight.mapper.FlightMapper;
import com.example.flight.flight.mapper.FlightSeatMapper;
import com.example.flight.flight.service.FlightService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
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
        // 缓存 key 不含 seatType：缓存"全舱位"结果，舱位过滤在内存做，命中率更高
        String cacheKey = String.format("FLIGHT:SEARCH:%s:%s:%s", departCity, arriveCity, date);

        List<Flight> flights = null;
        try {
            String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cachedJson)) {
                log.info("[Redis Cache Hit] 命中航班搜索缓存: key={}", cacheKey);
                flights = objectMapper.readValue(cachedJson, new TypeReference<List<Flight>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis 缓存查询异常，自动降级查 MySQL", e);
        }

        // 缓存未命中：查 MySQL（走 idx_route_date 索引）并回填缓存
        if (flights == null) {
            LambdaQueryWrapper<Flight> wrapper = new LambdaQueryWrapper<Flight>()
                    .eq(Flight::getFromCity, departCity)
                    .eq(Flight::getToCity, arriveCity)
                    .eq(Flight::getDepartDate, date)
                    .eq(Flight::getStatus, 1);
            flights = flightMapper.selectList(wrapper);

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
                log.info("[Redis Cache Write] 写入航班缓存成功: key={}, 共 {} 个航班", cacheKey, flights.size());
            } catch (Exception e) {
                log.error("写入 Redis 缓存失败", e);
            }
        }

        // 指定舱位时过滤舱位列表，并剔除已无该舱位的航班
        if (StringUtils.hasText(seatType)) {
            flights.forEach(f -> f.setSeats(
                    f.getSeats().stream().filter(s -> seatType.equals(s.getSeatType())).toList()));
            flights = flights.stream().filter(f -> !f.getSeats().isEmpty()).toList();
        }

        return flights;
    }
}
