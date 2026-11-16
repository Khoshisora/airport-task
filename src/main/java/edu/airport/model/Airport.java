package edu.airport.model;

import edu.airport.config.AirportConfig;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class Airport {

  private static final Logger LOGGER = LogManager.getLogger(Airport.class);
  private static final ReentrantLock INSTANCE_LOCK = new ReentrantLock();

  private static Airport instance;

  private final AirportFacilities facilities;

  private Airport(AirportFacilities facilities) {
    this.facilities = facilities;
  }

  public static Airport getInstance(AirportConfig config) {
    if (config == null) {
      throw new IllegalArgumentException("Airport configuration must not be null");
    }
    INSTANCE_LOCK.lock();
    try {
      if (instance == null) {
        instance = new Airport(new AirportFacilities(
            config.name(),
            config.gateCount(),
            config.terminalCapacity(),
            config.initialTerminalPassengers()));
        LOGGER.info("Airport singleton created for {}", config.name());
      }
      return instance;
    } finally {
      INSTANCE_LOCK.unlock();
    }
  }

  public AirportFacilities getFacilities() {
    return facilities;
  }

  public String getName() {
    return facilities.getName();
  }
}
