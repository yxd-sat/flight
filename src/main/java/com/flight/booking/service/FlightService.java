package com.flight.booking.service;

import com.flight.booking.entity.Flight;

import java.time.LocalDate;
import java.util.List;

public interface FlightService {

    List<Flight> searchFlights(String departCity, String arriveCity, LocalDate date, String seatType);
}
