package com.flight.booking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan("com.flight.booking.mapper")
public class FlightBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightBookingApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  机票预订系统服务启动成功！");
        System.out.println("  本地接口地址: http://localhost:8081");
        System.out.println("  Knife4j 文档: http://localhost:8081/doc.html");
        System.out.println("=================================================");
    }
}
