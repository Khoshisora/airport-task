package edu.airport.model.state;

import edu.airport.model.Plane;

public final class DepartedState implements PlaneState {

  @Override
  public String getName() {
    return "DEPARTED";
  }

  @Override
  public PlaneState proceed(Plane plane) {
    return this;
  }

  @Override
  public boolean isFinal() {
    return true;
  }
}
