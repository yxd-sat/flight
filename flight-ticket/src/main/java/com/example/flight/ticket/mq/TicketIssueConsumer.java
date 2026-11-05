package com.example.flight.ticket.mq;

import com.example.flight.order.entity.OrderInfo;
import com.example.flight.order.enums.OrderStatusEnum;
import com.example.flight.order.mapper.OrderInfoMapper;
import com.example.flight.ticket.entity.Ticket;
import com.example.flight.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "TICKET_ISSUE_TOPIC", consumerGroup = "ticket-issue-group")
public class TicketIssueConsumer implements RocketMQListener<String> {

    private final OrderInfoMapper orderInfoMapper;
    private final TicketMapper ticketMapper;

    @Override
    public void onMessage(String orderNo) {
        log.info("[RocketMQ 异步出票触发] 开始为订单出票: orderNo={}", orderNo);

        OrderInfo order = orderInfoMapper.selectByOrderNo(orderNo);
        if (order == null || order.getStatus() != OrderStatusEnum.PAID.getCode()) {
            log.warn("订单状态非已支付(1)，跳过出票: orderNo={}", orderNo);
            return;
        }

        String ticketNo = "999-" + (1000000000L + new Random().nextLong(9000000000L));
        String[] gates = {"A02", "B15", "C08", "D21"};
        String gate = gates[new Random().nextInt(gates.length)];
        String seatNo = (10 + new Random().nextInt(20)) + (order.getSeatType().equals("F") ? "A" : "K");

        Ticket ticket = new Ticket();
        ticket.setTicketNo(ticketNo);
        ticket.setOrderNo(orderNo);
        ticket.setUserId(order.getUserId());
        ticket.setFlightId(order.getFlightId());
        ticket.setPassengerName(order.getPassengerName());
        ticket.setSeatNo(seatNo);
        ticket.setBoardingGate(gate);
        ticket.setTicketStatus("ISSUED");
        ticket.setIssueTime(LocalDateTime.now());
        ticketMapper.insert(ticket);

        orderInfoMapper.updateStatus(orderNo, OrderStatusEnum.TICKET_ISSUED.getCode(), OrderStatusEnum.PAID.getCode());
        log.info("[出票成功] 电子客票已生成: ticketNo={}, 座位={}, 登机口={}, 订单状态更新为[已出票]", ticketNo, seatNo, gate);
    }
}
