package edu.airport.model.state;

import edu.airport.model.Gate;
import edu.airport.model.Plane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AwaitingGateState implements PlaneState {

  private static final Logger LOGGER = LogManager.getLogger(AwaitingGateState.class);

  @Override
  public String getName() {
    return "AWAITING_GATE";
  }

  @Override
  public PlaneState proceed(Plane plane) throws InterruptedException {
    LOGGER.info("Flight {} is waiting for a free gate", plane.getId());
    Gate gate = plane.getFacilities().getGatePool().acquire();
    plane.setGate(gate);
    LOGGER.info("Flight {} docked at gate {}", plane.getId(), gate.getNumber());
    return new DisembarkingState();
  }
}
