package com.flight.booking.controller;

import com.flight.booking.common.Result;
import com.flight.booking.entity.Flight;
import com.flight.booking.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flight")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping("/search")
    public Result<List<Flight>> search(@RequestParam String fromCity,
                                       @RequestParam String toCity,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                       @RequestParam(required = false) String seatType) {
        List<Flight> flights = flightService.searchFlights(fromCity, toCity, date, seatType);
        return Result.success(flights);
    }
}