package edu.airport.config;


public record PlaneConfig(
    String id,
    int capacity,
    int passengersOnBoard,
    int passengersToBoard,
    long serviceDelayMillis) {

  public PlaneConfig {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Flight id must not be blank");
    }
    if (capacity <= 0) {
      throw new IllegalArgumentException("Flight capacity must be positive");
    }
    if (passengersOnBoard < 0 || passengersOnBoard > capacity) {
      throw new IllegalArgumentException(
          "Passengers on board must be between 0 and flight capacity");
    }
    if (passengersToBoard < 0 || passengersToBoard > capacity) {
      throw new IllegalArgumentException(
          "Passengers to board must be between 0 and flight capacity");
    }
    if (serviceDelayMillis < 0) {
      throw new IllegalArgumentException("Service delay must not be negative");
    }
  }
}
