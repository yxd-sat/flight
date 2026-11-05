package com.flight.booking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("flight_seat")
public class FlightSeat implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long flightId;

    private String seatType;

    private String seatName;

    private BigDecimal price;

    private Integer stock;

    private Integer totalSeats;
}
