package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.Optional;
import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorReadable;

// readable interface for Road class
public interface RoadReadable {

  RoadId getRoadId();

  double getLength();

  JunctionId getIncomingJunctionId();

  JunctionId getOutgoingJunctionId();

  Optional<NeighborRoadInfo> getLeftNeighbor();

  /**
   * Returns the nearest car between the given one and the outgoing junction (i.e. in front of the given one).
   */
  Optional<CarReadable> getCarInFrontReadable(CarReadable car);

  /**
   * Returns the nearest car before given position
   */
  Optional<CarReadable> getCarBeforePosition(double position);

  /**
   * Returns the car closest to the incoming junction.
   */
  Optional<CarReadable> getCarAtEntryReadable();

  /**
   * Returns the car closest to the outgoing junction.
   */
  Optional<CarReadable> getCarAtExitReadable();

  /**
   * Returns a stream of cars, beginning from the one closest to the incoming junction.
   */
  Stream<CarReadable> streamCarsFromEntryReadable();

  /**
   * Returns a stream of cars, beginning from the one closest to the outgoing junction.
   */
  Stream<CarReadable> streamCarsFromExitReadable();

  /**
   * Returns number of vehicle in the current road
   */
  int numberOfCars();

  /**
   * Returns a traffic indicator which prohibits or allows entering incomingJunction
   */
  Optional<TrafficIndicatorReadable> getTrafficIndicator();
}
