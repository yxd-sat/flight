package com.example.flight.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_log")
public class PaymentLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private String payTradeNo;

    private BigDecimal payAmount;

    private String payChannel;

    private String payStatus;

    private LocalDateTime payTime;
}
