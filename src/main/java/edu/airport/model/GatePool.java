package edu.airport.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class GatePool {

  private static final Logger LOGGER = LogManager.getLogger(GatePool.class);

  private final int gateCount;
  private final List<Gate> gates;
  private final Deque<Gate> availableGates;
  private final ReentrantLock lock = new ReentrantLock(true);
  private final Condition gateAvailable = lock.newCondition();

  public GatePool(int gateCount) {
    if (gateCount <= 0) {
      throw new IllegalArgumentException("Gate count must be positive");
    }
    this.gateCount = gateCount;
    List<Gate> created = new ArrayList<>(gateCount);
    for (int number = 1; number <= gateCount; number++) {
      created.add(new Gate(number));
    }
    this.gates = List.copyOf(created);
    this.availableGates = new ArrayDeque<>(created);
  }

  public Gate acquire() throws InterruptedException {
    lock.lockInterruptibly();
    try {
      while (availableGates.isEmpty()) {
        LOGGER.debug("All {} gates are busy, waiting for a release", gateCount);
        gateAvailable.await();
      }
      Gate gate = availableGates.removeFirst();
      gate.occupy();
      LOGGER.debug("{} assigned", gate);
      return gate;
    } finally {
      lock.unlock();
    }
  }

  public void release(Gate gate) {
    if (gate == null) {
      throw new IllegalArgumentException("Gate must not be null");
    }
    lock.lock();
    try {
      if (!gates.contains(gate)) {
        throw new IllegalArgumentException("Gate does not belong to this pool");
      }
      gate.release();
      availableGates.addLast(gate);
      gateAvailable.signal();
      LOGGER.debug("{} released", gate);
    } finally {
      lock.unlock();
    }
  }

  public int getGateCount() {
    return gateCount;
  }

  public int getAvailableGateCount() {
    lock.lock();
    try {
      return availableGates.size();
    } finally {
      lock.unlock();
    }
  }
}
