package com.flight.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flight.booking.entity.PaymentLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PaymentLogMapper extends BaseMapper<PaymentLog> {

    @Select("SELECT * FROM payment_log WHERE pay_trade_no = #{payTradeNo} LIMIT 1")
    PaymentLog selectByTradeNo(@Param("payTradeNo") String payTradeNo);
}
