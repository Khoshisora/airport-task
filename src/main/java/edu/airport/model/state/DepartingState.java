package edu.airport.model.state;

import edu.airport.model.Gate;
import edu.airport.model.Plane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DepartingState implements PlaneState {

  private static final Logger LOGGER = LogManager.getLogger(DepartingState.class);

  @Override
  public String getName() {
    return "DEPARTING";
  }

  @Override
  public PlaneState proceed(Plane plane) {
    Gate gate = plane.getGate();
    if (gate != null) {
      plane.getFacilities().getGatePool().release(gate);
      plane.clearGate();
      LOGGER.info("Flight {} left gate {} and departed", plane.getId(), gate.getNumber());
    }
    return new DepartedState();
  }
}
