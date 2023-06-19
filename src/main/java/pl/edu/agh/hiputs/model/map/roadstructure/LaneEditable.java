package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.Optional;
import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.car.CarEditable;

/**
 * Editable interface for Lane class + readable interface
 */
public interface LaneEditable extends LaneReadable {

  /**
   * Adds a car to the set of cars intending to enter the lane.
   */
  void addIncomingCar(CarEditable car);

  /**
   * Adds a car to the lane, from the side of the incoming junction.
   */
  void addCarAtEntry(CarEditable car);

  /**
   * Add new car into lane
   */
  void addNewCar(CarEditable car);

  /**
   * Removes and returns the car from the lane that is closest to the outgoing junction.
   */
  Optional<CarEditable> pollCarAtExit();

  /**
   * Returns the car from the lane that is closest to the outgoing junction.
   */
  Optional<CarEditable> getCarAtExit();

  /**
   * Returns a stream of cars intending to enter the lane, removing them from the collection.
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

  /**
   * Remove all cars from list
   */
  void removeAllCars();

  /**
   * Updates attributes related to speed metrics
   */
  void updateCarSpeedMetrics();

  /**
   * Places a Car in Lane's list of cars based on defined Car's position
   *
   * @param car
   */
  void placeCarInQueueMiddle(CarEditable car);
}
