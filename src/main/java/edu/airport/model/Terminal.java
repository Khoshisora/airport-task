package edu.airport.model;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class Terminal {

  private static final Logger LOGGER = LogManager.getLogger(Terminal.class);

  private final int capacity;
  private final ReentrantLock lock = new ReentrantLock(true);
  private final Condition stateChanged = lock.newCondition();

  private int passengers;
  private int reservedIncoming;
  private int reservedOutgoing;

  public Terminal(int capacity, int initialPassengers) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Terminal capacity must be positive");
    }
    if (initialPassengers < 0 || initialPassengers > capacity) {
      throw new IllegalArgumentException(
          "Initial passengers must be between 0 and terminal capacity");
    }
    this.capacity = capacity;
    this.passengers = initialPassengers;
  }

  public TerminalReservation reserve(int disembarking, int boarding)
      throws InterruptedException {
    if (disembarking < 0 || boarding < 0) {
      throw new IllegalArgumentException("Passenger counts must not be negative");
    }

    lock.lockInterruptibly();
    try {
      while (!canReserve(disembarking, boarding)) {
        LOGGER.debug("Terminal {}/{} is not ready for {} disembarking / {} boarding, waiting",
            passengers, capacity, disembarking, boarding);
        stateChanged.await();
      }
      reservedIncoming += disembarking;
      reservedOutgoing += boarding;
      checkInvariants();
      LOGGER.debug("Reserved {} disembarking / {} boarding; terminal {}/{}, incoming {}/outgoing {}",
          disembarking, boarding, passengers, capacity, reservedIncoming, reservedOutgoing);
      return new TerminalReservation(disembarking, boarding);
    } finally {
      lock.unlock();
    }
  }

  public void completeDisembarkation(TerminalReservation reservation) {
    if (reservation == null) {
      throw new IllegalArgumentException("Reservation must not be null");
    }
    lock.lock();
    try {
      int count = reservation.completeDisembarkation();
      reservedIncoming -= count;
      passengers += count;
      checkInvariants();
      stateChanged.signalAll();
      LOGGER.debug("Applied disembarkation of {} passengers; terminal {}/{}",
          count, passengers, capacity);
    } finally {
      lock.unlock();
    }
  }

  public void completeBoarding(TerminalReservation reservation) {
    if (reservation == null) {
      throw new IllegalArgumentException("Reservation must not be null");
    }
    lock.lock();
    try {
      int count = reservation.completeBoarding();
      reservedOutgoing -= count;
      passengers -= count;
      checkInvariants();
      stateChanged.signalAll();
      LOGGER.debug("Applied boarding of {} passengers; terminal {}/{}",
          count, passengers, capacity);
    } finally {
      lock.unlock();
    }
  }

  public void cancel(TerminalReservation reservation) {
    if (reservation == null) {
      return;
    }
    lock.lock();
    try {
      int incoming = reservation.cancelIncoming();
      int outgoing = reservation.cancelOutgoing();
      if (incoming == 0 && outgoing == 0) {
        return;
      }
      reservedIncoming -= incoming;
      reservedOutgoing -= outgoing;
      checkInvariants();
      stateChanged.signalAll();
      LOGGER.debug("Cancelled reservation: {} incoming / {} outgoing; terminal {}/{}",
          incoming, outgoing, passengers, capacity);
    } finally {
      lock.unlock();
    }
  }

  public TerminalSnapshot snapshot() {
    lock.lock();
    try {
      return new TerminalSnapshot(capacity, passengers);
    } finally {
      lock.unlock();
    }
  }

  public int getCapacity() {
    return capacity;
  }

  private boolean canReserve(int disembarking, int boarding) {
    long occupancyWithReserved = (long) passengers + reservedIncoming + disembarking;
    return occupancyWithReserved <= capacity
        && (long) passengers - reservedOutgoing >= boarding;
  }

  private void checkInvariants() {
    long occupancyWithReserved = (long) passengers + reservedIncoming;
    boolean valid = passengers >= 0
        && passengers <= capacity
        && reservedIncoming >= 0
        && reservedOutgoing >= 0
        && occupancyWithReserved <= capacity
        && passengers >= reservedOutgoing;
    if (!valid) {
      throw new IllegalStateException("Terminal state is inconsistent: passengers=" + passengers
          + ", capacity=" + capacity + ", incoming=" + reservedIncoming
          + ", outgoing=" + reservedOutgoing);
    }
  }
}
