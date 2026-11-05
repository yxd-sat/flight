package com.flight.booking.service.impl;

import com.flight.booking.common.BusinessException;
import com.flight.booking.common.OrderStatusEnum;
import com.flight.booking.entity.OrderInfo;
import com.flight.booking.entity.PaymentLog;
import com.flight.booking.mapper.OrderInfoMapper;
import com.flight.booking.mapper.PaymentLogMapper;
import com.flight.booking.mq.TicketIssueProducer;
import com.flight.booking.service.PayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private final OrderInfoMapper orderInfoMapper;
    private final PaymentLogMapper paymentLogMapper;
    private final TicketIssueProducer ticketIssueProducer;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentCallback(String orderNo, String payTradeNo, String payChannel) {
        // 1. 幂等性控制：基于 Redis SETNX 防并发重复请求
        String lockKey = "PAY_CALLBACK_LOCK:" + payTradeNo;
        Boolean isFirst = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofMinutes(5));
        if (Boolean.FALSE.equals(isFirst)) {
            log.warn("[幂等防护] 支付回调正在处理中或已处理: tradeNo={}", payTradeNo);
            return true;
        }

        // 2. 数据库唯一键校验防重
        PaymentLog existLog = paymentLogMapper.selectByTradeNo(payTradeNo);
        if (existLog != null) {
            log.info("[幂等校验通过] 交易流水已存在，直接返回成功: tradeNo={}", payTradeNo);
            return true;
        }

        // 3. 校验订单当前状态 (必须为 0 待支付)
        OrderInfo order = orderInfoMapper.selectByOrderNo(orderNo);
        if (order == null) throw new BusinessException("关联订单不存在: " + orderNo);

        if (order.getStatus() == OrderStatusEnum.PAID.getCode() || order.getStatus() == OrderStatusEnum.TICKET_ISSUED.getCode()) {
            log.info("订单已支付或已出票，无需重复推进: orderNo={}", orderNo);
            return true;
        }

        if (!OrderStatusEnum.isValidTransition(order.getStatus(), OrderStatusEnum.PAID.getCode())) {
            throw new BusinessException(String.format("订单状态异常(当前状态:%d)，支付回调推进失败", order.getStatus()));
        }

        // 4. 插入支付流水记录
        PaymentLog paymentLog = new PaymentLog();
        paymentLog.setOrderNo(orderNo);
        paymentLog.setPayTradeNo(payTradeNo);
        paymentLog.setPayAmount(order.getOrderAmount());
        paymentLog.setPayChannel(payChannel);
        paymentLog.setPayStatus("SUCCESS");
        paymentLog.setPayTime(LocalDateTime.now());
        paymentLogMapper.insert(paymentLog);

        // 5. 推进订单状态为 1 (已支付)
        orderInfoMapper.updateStatus(orderNo, OrderStatusEnum.PAID.getCode(), OrderStatusEnum.PENDING_PAY.getCode());

        // 6. 发送异步出票消息 (RocketMQ 生产端)
        ticketIssueProducer.sendTicketIssueMessage(orderNo);
        log.info("支付回调成功，已发送异步出票消息: orderNo={}", orderNo);

        return true;
    }
}
