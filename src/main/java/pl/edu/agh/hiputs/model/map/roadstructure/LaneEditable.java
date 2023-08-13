package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.Optional;
import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.car.CarEditable;

public interface LaneEditable extends LaneReadable{

  /**
   * Adds a car to the set of cars intending to enter the road.
   */
  void addIncomingCar(CarEditable car);

  /**
   * Adds a car to the road, from the side of the incoming junction.
   */
  void addCarAtEntry(CarEditable car);

  /**
   * Add new car into road
   */
  void addNewCar(CarEditable car);

  /**
   * Adds a car to lane, if car changed lane (on the same road)
   */
  void addCarLaneChange(CarEditable car);

  /**
   * Removes and returns the car from the road that is closest to the outgoing junction.
   */
  Optional<CarEditable> pollCarAtExit();

  /**
   * Returns the car from the road that is closest to the outgoing junction.
   */
  Optional<CarEditable> getCarAtExit();

  /**
   * Returns a stream of cars intending to enter the road, removing them from the collection.
   */
  Stream<CarEditable> pollIncomingCars();

  /**
   * Returns a stream of cars, beginning from the one closest to the incoming junction.
   */
  Stream<CarEditable> streamCarsFromEntryEditable();

  /**
   * Returns a stream of cars, beginning from the one closest to the outgoing junction.
   */
  Stream<CarEditable> streamCarsFromExitEditable();

  /**
   * Remove specific car from list
   */
  boolean removeCar(CarEditable car);

}
