package com.example.flight.flight.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 航班舱位库存实体，对齐表 t_flight_seat（防超卖核心表）
 */
@Data
@TableName("t_flight_seat")
public class FlightSeat implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long flightId;

    /** Y 经济舱 / J 公务舱 / F 头等舱 */
    private String seatType;

    /** 票价，单位：分（避免金额浮点误差） */
    private Integer price;

    private Integer totalCount;

    /** 可售余票数 */
    private Integer remainCount;

    /** 乐观锁版本号 */
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
