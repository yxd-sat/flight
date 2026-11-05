package com.flight.booking.service;

public interface OrderService {

    /**
     * 创建订单 (Redis Lua 原子预扣 + 数据库乐观锁防超卖)
     */
    String createOrder(Long userId, Long flightId, String seatType, String passengerName, String idCard);

    /**
     * 取消订单 (主动取消或超时关单，归还库存)
     */
    void cancelOrder(String orderNo, String reason);
}
