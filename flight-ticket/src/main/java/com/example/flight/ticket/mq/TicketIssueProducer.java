package com.example.flight.ticket.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TicketIssueProducer {

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    public void sendTicketIssueMessage(String orderNo) {
        if (rocketMQTemplate == null) {
            log.warn("[RocketMQ 未初始化] 出票消息模拟跳过: orderNo={}", orderNo);
            return;
        }
        try {
            log.info("[RocketMQ 生产] 发送异步出票消息: orderNo={}", orderNo);
            rocketMQTemplate.convertAndSend("TICKET_ISSUE_TOPIC", orderNo);
        } catch (Exception e) {
            log.error("发送出票消息失败: {}", e.getMessage());
        }
    }
}
