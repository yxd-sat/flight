package com.example.flight.flight.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 航班实体，对齐表 t_flight
 */
@Data
@TableName("t_flight")
public class Flight implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 航班号，如 CA1234 */
    private String flightNo;

    private String airline;

    private Long fromAirportId;

    private Long toAirportId;

    /** 出发城市（冗余字段，加速按城市搜索） */
    private String fromCity;

    private String toCity;

    /** 航班日期 */
    private LocalDate departDate;

    /** 起飞时刻 */
    private LocalTime departTime;

    /** 到达时刻 */
    private LocalTime arriveTime;

    /** 1 在售 0 停售 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 航班下的舱位库存列表（非表字段，查询时组装） */
    @TableField(exist = false)
    private List<FlightSeat> seats;
}
