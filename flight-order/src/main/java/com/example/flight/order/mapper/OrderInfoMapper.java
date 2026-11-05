package com.example.flight.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.flight.order.entity.OrderInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    @Select("SELECT * FROM order_info WHERE order_no = #{orderNo} LIMIT 1")
    OrderInfo selectByOrderNo(@Param("orderNo") String orderNo);

    @Update("UPDATE order_info SET status = #{targetStatus}, update_time = NOW() " +
            "WHERE order_no = #{orderNo} AND status = #{expectStatus}")
    int updateStatus(@Param("orderNo") String orderNo,
                     @Param("targetStatus") int targetStatus,
                     @Param("expectStatus") int expectStatus);
}
