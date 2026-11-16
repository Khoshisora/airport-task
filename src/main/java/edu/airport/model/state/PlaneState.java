package edu.airport.model.state;

import edu.airport.model.Plane;


public interface PlaneState {

  String getName();

  PlaneState proceed(Plane plane) throws InterruptedException;

  default boolean isFinal() {
    return false;
  }
}
