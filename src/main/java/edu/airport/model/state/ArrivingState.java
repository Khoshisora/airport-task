package edu.airport.model.state;

import edu.airport.model.Plane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ArrivingState implements PlaneState {

  private static final Logger LOGGER = LogManager.getLogger(ArrivingState.class);

  @Override
  public String getName() {
    return "ARRIVING";
  }

  @Override
  public PlaneState proceed(Plane plane) {
    LOGGER.info("Flight {} requests service: {} passengers to disembark, {} to board",
        plane.getId(), plane.getPassengersToDisembark(), plane.getPassengersToBoard());
    return new AwaitingTerminalState();
  }
}
