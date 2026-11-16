package edu.airport.model.state;

import edu.airport.model.Plane;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DisembarkingState implements PlaneState {

  private static final Logger LOGGER = LogManager.getLogger(DisembarkingState.class);

  @Override
  public String getName() {
    return "DISEMBARKING";
  }

  @Override
  public PlaneState proceed(Plane plane) throws InterruptedException {
    int passengers = plane.getPassengersToDisembark();
    if (passengers > 0) {
      LOGGER.info("Flight {} starts disembarking {} passengers at gate {}",
          plane.getId(), passengers, plane.getGate().getNumber());
      TimeUnit.MILLISECONDS.sleep(plane.getServiceDelayMillis());
      plane.getFacilities().getTerminal().completeDisembarkation(plane.getReservation());
      plane.markDisembarked();
      LOGGER.info("Flight {} finished disembarking", plane.getId());
    }
    return new BoardingState();
  }
}
