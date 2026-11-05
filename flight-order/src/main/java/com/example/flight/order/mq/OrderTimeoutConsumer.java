package com.example.flight.order.mq;

import com.example.flight.flight.mapper.FlightSeatMapper;
import com.example.flight.order.entity.OrderInfo;
import com.example.flight.order.enums.OrderStatusEnum;
import com.example.flight.order.mapper.OrderInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "ORDER_TIMEOUT_TOPIC", consumerGroup = "order-timeout-group")
public class OrderTimeoutConsumer implements RocketMQListener<String> {

    private final OrderInfoMapper orderInfoMapper;
    private final FlightSeatMapper flightSeatMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(String orderNo) {
        log.info("[RocketMQ 延时关单触发] 接收到延时消息, 准备检查订单: orderNo={}", orderNo);

        OrderInfo order = orderInfoMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.warn("订单不存在: orderNo={}", orderNo);
            return;
        }

        if (order.getStatus() == OrderStatusEnum.PENDING_PAY.getCode()) {
            int updated = orderInfoMapper.updateStatus(orderNo, OrderStatusEnum.CANCELLED.getCode(), OrderStatusEnum.PENDING_PAY.getCode());
            if (updated > 0) {
                flightSeatMapper.restoreStock(order.getFlightId(), order.getSeatType(), 1);
                String stockKey = String.format("FLIGHT:STOCK:%d:%s", order.getFlightId(), order.getSeatType());
                stringRedisTemplate.opsForValue().increment(stockKey, 1);
                log.info("[超时关单成功] 15分钟未支付，订单已取消并完成库存归还: orderNo={}", orderNo);
            }
        } else {
            log.info("订单已处理(状态:{})，无需超时取消: orderNo={}", order.getStatus(), orderNo);
        }
    }
}
