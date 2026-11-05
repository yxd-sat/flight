package com.example.flight.flight.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.flight.flight.entity.FlightSeat;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface FlightSeatMapper extends BaseMapper<FlightSeat> {

    @Select("SELECT * FROM t_flight_seat WHERE flight_id = #{flightId} AND seat_type = #{seatType} LIMIT 1")
    FlightSeat selectOneByFlightAndType(@Param("flightId") Long flightId, @Param("seatType") String seatType);

    @Update("UPDATE t_flight_seat SET remain_count = remain_count - #{count} " +
            "WHERE flight_id = #{flightId} AND seat_type = #{seatType} AND remain_count >= #{count}")
    int deductStockOptimistic(@Param("flightId") Long flightId,
                              @Param("seatType") String seatType,
                              @Param("count") int count);

    @Update("UPDATE t_flight_seat SET remain_count = remain_count + #{count} " +
            "WHERE flight_id = #{flightId} AND seat_type = #{seatType}")
    int restoreStock(@Param("flightId") Long flightId,
                     @Param("seatType") String seatType,
                     @Param("count") int count);
}
