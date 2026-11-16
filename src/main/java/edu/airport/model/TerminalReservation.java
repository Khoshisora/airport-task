package edu.airport.model;


public final class TerminalReservation {

  private final int disembarkingPassengers;
  private final int boardingPassengers;
  private int pendingIncoming;
  private int pendingOutgoing;
  private boolean incomingCompleted;
  private boolean outgoingCompleted;

  TerminalReservation(int disembarkingPassengers, int boardingPassengers) {
    this.disembarkingPassengers = disembarkingPassengers;
    this.boardingPassengers = boardingPassengers;
    this.pendingIncoming = disembarkingPassengers;
    this.pendingOutgoing = boardingPassengers;
  }

  public int getDisembarkingPassengers() {
    return disembarkingPassengers;
  }

  public int getBoardingPassengers() {
    return boardingPassengers;
  }

  int completeDisembarkation() {
    if (incomingCompleted) {
      throw new IllegalStateException("Disembarkation has already been completed");
    }
    incomingCompleted = true;
    return takePendingIncoming();
  }

  int completeBoarding() {
    if (outgoingCompleted) {
      throw new IllegalStateException("Boarding has already been completed");
    }
    outgoingCompleted = true;
    return takePendingOutgoing();
  }

  int cancelIncoming() {
    if (incomingCompleted) {
      return 0;
    }
    incomingCompleted = true;
    return takePendingIncoming();
  }

  int cancelOutgoing() {
    if (outgoingCompleted) {
      return 0;
    }
    outgoingCompleted = true;
    return takePendingOutgoing();
  }

  private int takePendingIncoming() {
    int value = pendingIncoming;
    pendingIncoming = 0;
    return value;
  }

  private int takePendingOutgoing() {
    int value = pendingOutgoing;
    pendingOutgoing = 0;
    return value;
  }
}
