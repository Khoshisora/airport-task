package edu.airport.model;


public record PlaneReport(
    String flightId,
    int gateNumber,
    int disembarkedPassengers,
    int boardedPassengers,
    long serviceMillis) {

  public PlaneReport {
    if (flightId == null || flightId.isBlank()) {
      throw new IllegalArgumentException("Flight id must not be blank");
    }
    if (gateNumber <= 0) {
      throw new IllegalArgumentException("Gate number must be positive");
    }
    if (disembarkedPassengers < 0 || boardedPassengers < 0) {
      throw new IllegalArgumentException("Passenger counts must not be negative");
    }
    if (serviceMillis < 0) {
      throw new IllegalArgumentException("Service duration must not be negative");
    }
  }
}
