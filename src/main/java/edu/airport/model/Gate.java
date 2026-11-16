package edu.airport.model;


public final class Gate {

  private final int number;
  private boolean free = true;

  Gate(int number) {
    this.number = number;
  }

  public int getNumber() {
    return number;
  }

  void occupy() {
    if (!free) {
      throw new IllegalStateException("Gate " + number + " is already occupied");
    }
    free = false;
  }

  void release() {
    if (free) {
      throw new IllegalStateException("Gate " + number + " is already free");
    }
    free = true;
  }

  @Override
  public String toString() {
    return "gate " + number;
  }
}
