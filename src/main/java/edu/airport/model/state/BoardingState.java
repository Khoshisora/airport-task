package edu.airport.model.state;

import edu.airport.model.Plane;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BoardingState implements PlaneState {

  private static final Logger LOGGER = LogManager.getLogger(BoardingState.class);

  @Override
  public String getName() {
    return "BOARDING";
  }

  @Override
  public PlaneState proceed(Plane plane) throws InterruptedException {
    int passengers = plane.getPassengersToBoard();
    if (passengers > 0) {
      LOGGER.info("Flight {} starts boarding {} passengers at gate {}",
          plane.getId(), passengers, plane.getGate().getNumber());
      TimeUnit.MILLISECONDS.sleep(plane.getServiceDelayMillis());
      plane.getFacilities().getTerminal().completeBoarding(plane.getReservation());
      plane.markBoarded();
      LOGGER.info("Flight {} finished boarding", plane.getId());
    }
    return new DepartingState();
  }
}
