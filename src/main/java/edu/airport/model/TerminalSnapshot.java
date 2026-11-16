package edu.airport.model;


public record TerminalSnapshot(int capacity, int passengers) {

  public TerminalSnapshot {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Terminal capacity must be positive");
    }
    if (passengers < 0 || passengers > capacity) {
      throw new IllegalArgumentException("Passengers must be between 0 and terminal capacity");
    }
  }
}
