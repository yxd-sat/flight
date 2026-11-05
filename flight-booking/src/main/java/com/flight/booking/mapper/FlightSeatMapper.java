package com.flight.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flight.booking.entity.FlightSeat;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface FlightSeatMapper extends BaseMapper<FlightSeat> {

    @Select("SELECT * FROM flight_seat WHERE flight_id = #{flightId} AND seat_type = #{seatType} LIMIT 1")
    FlightSeat selectOneByFlightAndType(@Param("flightId") Long flightId, @Param("seatType") String seatType);

    @Update("UPDATE flight_seat SET stock = stock - #{count} " +
            "WHERE flight_id = #{flightId} AND seat_type = #{seatType} AND stock >= #{count}")
    int deductStockOptimistic(@Param("flightId") Long flightId,
                              @Param("seatType") String seatType,
                              @Param("count") int count);

    @Update("UPDATE flight_seat SET stock = stock + #{count} " +
            "WHERE flight_id = #{flightId} AND seat_type = #{seatType}")
    int restoreStock(@Param("flightId") Long flightId,
                     @Param("seatType") String seatType,
                     @Param("count") int count);
}
