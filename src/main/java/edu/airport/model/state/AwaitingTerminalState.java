package edu.airport.model.state;

import edu.airport.model.Plane;
import edu.airport.model.TerminalReservation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AwaitingTerminalState implements PlaneState {

  private static final Logger LOGGER = LogManager.getLogger(AwaitingTerminalState.class);

  @Override
  public String getName() {
    return "AWAITING_TERMINAL";
  }

  @Override
  public PlaneState proceed(Plane plane) throws InterruptedException {
    LOGGER.info("Flight {} is waiting for terminal resources", plane.getId());
    TerminalReservation reservation = plane.getFacilities().getTerminal()
        .reserve(plane.getPassengersToDisembark(), plane.getPassengersToBoard());
    plane.setReservation(reservation);
    LOGGER.info("Flight {} reserved terminal: {} disembarking / {} boarding",
        plane.getId(), reservation.getDisembarkingPassengers(),
        reservation.getBoardingPassengers());
    return new AwaitingGateState();
  }
}
