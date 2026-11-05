package com.example.flight.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ticket")
public class Ticket implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ticketNo;

    private String orderNo;

    private Long userId;

    private Long flightId;

    private String passengerName;

    private String seatNo;

    private String boardingGate;

    private String ticketStatus;

    private LocalDateTime issueTime;
}
