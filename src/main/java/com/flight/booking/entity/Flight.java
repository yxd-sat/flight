package com.flight.booking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("flight")
public class Flight implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String flightNo;

    private String airlineName;

    private String departCity;

    private String arriveCity;

    private String departAirport;

    private String arriveAirport;

    private LocalDateTime departTime;

    private LocalDateTime arriveTime;

    private String aircraftModel;

    private Integer status;

    @TableField(exist = false)
    private List<FlightSeat> seats;
}
