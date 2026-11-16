package edu.airport.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public record AirportConfig(
    String name,
    int gateCount,
    int terminalCapacity,
    int initialTerminalPassengers,
    List<PlaneConfig> planes) {

  public AirportConfig {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Airport name must not be blank");
    }
    if (gateCount <= 0) {
      throw new IllegalArgumentException("Gate count must be positive");
    }
    if (terminalCapacity <= 0) {
      throw new IllegalArgumentException("Terminal capacity must be positive");
    }
    if (initialTerminalPassengers < 0 || initialTerminalPassengers > terminalCapacity) {
      throw new IllegalArgumentException(
          "Initial terminal passengers must be between 0 and terminal capacity");
    }
    if (planes == null || planes.isEmpty()) {
      throw new IllegalArgumentException("At least one flight must be scheduled");
    }

    Set<String> flightIds = new HashSet<>();
    for (PlaneConfig plane : planes) {
      if (plane == null) {
        throw new IllegalArgumentException("Flight list must not contain null entries");
      }
      if (!flightIds.add(plane.id())) {
        throw new IllegalArgumentException("Duplicate flight id: " + plane.id());
      }
    }
    planes = List.copyOf(planes);
  }
}
