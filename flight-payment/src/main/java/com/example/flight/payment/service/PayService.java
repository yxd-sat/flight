package com.example.flight.payment.service;

public interface PayService {

    /**
     * 处理第三方支付回调 (具备幂等性与状态机校验)
     * @param orderNo 订单编号
     * @param payTradeNo 第三方支付流水号
     * @param payChannel 支付渠道 (ALIPAY / WECHAT / MOCK)
     * @return 是否处理成功
     */
    boolean handlePaymentCallback(String orderNo, String payTradeNo, String payChannel);
}
