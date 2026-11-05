package com.flight.booking.service.impl;

import com.flight.booking.common.BusinessException;
import com.flight.booking.common.OrderStatusEnum;
import com.flight.booking.entity.FlightSeat;
import com.flight.booking.entity.OrderInfo;
import com.flight.booking.mapper.FlightSeatMapper;
import com.flight.booking.mapper.OrderInfoMapper;
import com.flight.booking.mq.OrderTimeoutProducer;
import com.flight.booking.service.OrderService;
import com.flight.booking.utils.SnowflakeIdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final FlightSeatMapper flightSeatMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final SnowflakeIdWorker snowflakeIdWorker;
    private final OrderTimeoutProducer orderTimeoutProducer;

    private static final String PRE_DEDUCT_LUA =
            "local stock = tonumber(redis.call('get', KEYS[1])) " +
            "if (stock and stock >= tonumber(ARGV[1])) then " +
            "    redis.call('decrby', KEYS[1], ARGV[1]) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, Long flightId, String seatType, String passengerName, String idCard) {
        String stockKey = String.format("FLIGHT:STOCK:%d:%s", flightId, seatType);

        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(stockKey))) {
            FlightSeat dbSeat = flightSeatMapper.selectOneByFlightAndType(flightId, seatType);
            if (dbSeat == null) throw new BusinessException("该航班舱位不存在");
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(dbSeat.getStock()));
        }

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(PRE_DEDUCT_LUA, Long.class);
        Long result = stringRedisTemplate.execute(redisScript, Collections.singletonList(stockKey), "1");

        if (result == null || result == 0) {
            log.warn("Redis 预扣库存失败，座位已售罄: flightId={}, seatType={}", flightId, seatType);
            throw new BusinessException("该舱位已被抢空，请选择其他航班或舱位！");
        }

        int updated = flightSeatMapper.deductStockOptimistic(flightId, seatType, 1);
        if (updated <= 0) {
            stringRedisTemplate.opsForValue().increment(stockKey, 1);
            throw new BusinessException("余票不足，下单失败！");
        }

        FlightSeat seat = flightSeatMapper.selectOneByFlightAndType(flightId, seatType);
        String orderNo = String.valueOf(snowflakeIdWorker.nextId());
        LocalDateTime now = LocalDateTime.now();

        OrderInfo order = new OrderInfo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setFlightId(flightId);
        order.setSeatType(seatType);
        order.setPassengerName(passengerName);
        order.setPassengerIdCard(idCard);
        order.setOrderAmount(seat.getPrice());
        order.setStatus(OrderStatusEnum.PENDING_PAY.getCode());
        order.setExpireTime(now.plusMinutes(15));
        order.setCreateTime(now);

        orderInfoMapper.insert(order);

        orderTimeoutProducer.sendDelayOrderMessage(orderNo, 15 * 60 * 1000L);

        log.info("订单创建成功: orderNo={}, userId={}, 状态=待支付(0)", orderNo, userId);
        return orderNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, String reason) {
        OrderInfo order = orderInfoMapper.selectByOrderNo(orderNo);
        if (order == null) throw new BusinessException("订单不存在");

        if (!OrderStatusEnum.isValidTransition(order.getStatus(), OrderStatusEnum.CANCELLED.getCode())) {
            throw new BusinessException(String.format("订单当前状态为[%s]，无法执行取消操作",
                    OrderStatusEnum.getByCode(order.getStatus()).getDesc()));
        }

        int updated = orderInfoMapper.updateStatus(orderNo, OrderStatusEnum.CANCELLED.getCode(), order.getStatus());
        if (updated > 0) {
            flightSeatMapper.restoreStock(order.getFlightId(), order.getSeatType(), 1);
            String stockKey = String.format("FLIGHT:STOCK:%d:%s", order.getFlightId(), order.getSeatType());
            stringRedisTemplate.opsForValue().increment(stockKey, 1);
            log.info("订单取消成功，库存已安全归还: orderNo={}, 原因={}", orderNo, reason);
        }
    }
}
