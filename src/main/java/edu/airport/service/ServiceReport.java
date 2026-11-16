package edu.airport.service;

import edu.airport.model.PlaneReport;
import edu.airport.model.TerminalSnapshot;
import java.util.List;


public record ServiceReport(
    String airportName,
    int gateCount,
    TerminalSnapshot terminalBefore,
    TerminalSnapshot terminalAfter,
    List<PlaneReport> flights,
    long durationMillis) {

  public ServiceReport {
    if (airportName == null || airportName.isBlank()) {
      throw new IllegalArgumentException("Airport name must not be blank");
    }
    if (gateCount <= 0) {
      throw new IllegalArgumentException("Gate count must be positive");
    }
    if (terminalBefore == null || terminalAfter == null) {
      throw new IllegalArgumentException("Terminal snapshots must not be null");
    }
    if (flights == null || flights.isEmpty()) {
      throw new IllegalArgumentException("Served flights must not be empty");
    }
    if (durationMillis < 0) {
      throw new IllegalArgumentException("Duration must not be negative");
    }
    flights = List.copyOf(flights);
  }
}
