package edu.airport.model;

import edu.airport.config.PlaneConfig;
import edu.airport.exception.AirportException;
import edu.airport.model.state.AwaitingTerminalState;
import edu.airport.model.state.PlaneState;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class Plane implements Callable<PlaneReport> {

  private static final Logger LOGGER = LogManager.getLogger(Plane.class);

  private final PlaneConfig schedule;
  private final AirportFacilities facilities;

  private PlaneState state = new AwaitingTerminalState();
  private TerminalReservation reservation;
  private Gate gate;
  private int gateNumber;
  private int disembarkedPassengers;
  private int boardedPassengers;

  public Plane(PlaneConfig schedule, AirportFacilities facilities) {
    if (schedule == null) {
      throw new IllegalArgumentException("Flight schedule must not be null");
    }
    if (facilities == null) {
      throw new IllegalArgumentException("Airport facilities must not be null");
    }
    this.schedule = schedule;
    this.facilities = facilities;
  }

  @Override
  public PlaneReport call() throws AirportException {
    long startedAt = System.nanoTime();
    LOGGER.info("Flight {} started service at airport {}: {} to disembark, {} to board",
        getId(), facilities.getName(), getPassengersToDisembark(), getPassengersToBoard());
    try {
      while (!state.isFinal()) {
        PlaneState nextState = state.proceed(this);
        changeState(nextState);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error("Flight {} was interrupted in state {}", getId(), state.getName());
      throw new AirportException("Flight " + getId() + " was interrupted during service", e);
    } finally {
      releaseResources();
    }

    long serviceMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    LOGGER.info("Flight {} finished service in {} ms at gate {}",
        getId(), serviceMillis, gateNumber);
    return new PlaneReport(getId(), gateNumber, disembarkedPassengers, boardedPassengers,
        serviceMillis);
  }

  public String getId() {
    return schedule.id();
  }

  public AirportFacilities getFacilities() {
    return facilities;
  }

  public int getPassengersToDisembark() {
    return schedule.passengersOnBoard();
  }

  public int getPassengersToBoard() {
    return schedule.passengersToBoard();
  }

  public long getServiceDelayMillis() {
    return schedule.serviceDelayMillis();
  }

  public TerminalReservation getReservation() {
    return reservation;
  }

  public void setReservation(TerminalReservation reservation) {
    this.reservation = reservation;
  }

  public Gate getGate() {
    return gate;
  }

  public void setGate(Gate gate) {
    this.gate = gate;
    this.gateNumber = gate.getNumber();
  }

  public void clearGate() {
    this.gate = null;
  }

  public void markDisembarked() {
    disembarkedPassengers = schedule.passengersOnBoard();
  }

  public void markBoarded() {
    boardedPassengers = schedule.passengersToBoard();
  }

  public PlaneState getState() {
    return state;
  }

  private void changeState(PlaneState nextState) {
    if (nextState == null) {
      throw new IllegalStateException("Plane state must not be null");
    }
    LOGGER.debug("Flight {} state {} -> {}", getId(), state.getName(), nextState.getName());
    state = nextState;
  }

  private void releaseResources() {
    if (gate != null) {
      facilities.getGatePool().release(gate);
      gate = null;
    }
    if (reservation != null) {
      facilities.getTerminal().cancel(reservation);
      reservation = null;
    }
  }
}
