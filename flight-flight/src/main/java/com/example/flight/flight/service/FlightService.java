package com.example.flight.flight.service;

import com.example.flight.flight.entity.Flight;

import java.time.LocalDate;
import java.util.List;

public interface FlightService {

    List<Flight> searchFlights(String departCity, String arriveCity, LocalDate date, String seatType);
}
