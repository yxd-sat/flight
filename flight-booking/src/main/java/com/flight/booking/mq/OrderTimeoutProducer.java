package com.flight.booking.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTimeoutProducer {

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    public void sendDelayOrderMessage(String orderNo, long timeoutMs) {
        if (rocketMQTemplate == null) {
            log.warn("[RocketMQ 未初始化] 延时关单消息模拟跳过: orderNo={}", orderNo);
            return;
        }
        try {
            log.info("[RocketMQ 生产] 发送订单超时延时消息: orderNo={}, 预计15分钟后触发", orderNo);
            rocketMQTemplate.syncSend(
                    "ORDER_TIMEOUT_TOPIC",
                    MessageBuilder.withPayload(orderNo).build(),
                    3000,
                    14 // 15分钟延时级别
            );
        } catch (Exception e) {
            log.error("发送 RocketMQ 延时消息失败: {}", e.getMessage());
        }
    }
}