package edu.airport.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class AirportFacilities {

  private static final Logger LOGGER = LogManager.getLogger(AirportFacilities.class);

  private final String name;
  private final Terminal terminal;
  private final GatePool gatePool;

  public AirportFacilities(String name, int gateCount, int terminalCapacity,
      int initialTerminalPassengers) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Airport name must not be blank");
    }
    this.name = name;
    this.terminal = new Terminal(terminalCapacity, initialTerminalPassengers);
    this.gatePool = new GatePool(gateCount);
    LOGGER.info("Airport {} initialized: {} gates, terminal {}/{} passengers",
        name, gateCount, initialTerminalPassengers, terminalCapacity);
  }

  public String getName() {
    return name;
  }

  public Terminal getTerminal() {
    return terminal;
  }

  public GatePool getGatePool() {
    return gatePool;
  }
}
