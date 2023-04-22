package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;

@Slf4j
@Builder
@AllArgsConstructor
public class Road implements RoadEditable {

  /**
   * Unique road identifier.
   */
  @Getter
  private final RoadId roadId;

  /**
   * Collection of cars traveling on this road.
   */
  @Builder.Default
  private final Deque<CarEditable> cars = new LinkedList<>();

  /**
   * Reference to road that is on the left side of this one, for now it should be in opposite direction.
   */
  @Getter
  @Builder.Default
  private final Optional<NeighborRoadInfo> leftNeighbor = Optional.empty();

  /**
   * Reference to junction id that is at the begging of road
   * j --------->
   */
  @Getter
  private final JunctionId incomingJunctionId;

  /**
   * Reference to junction id that is at the end of road
   * ---------> j
   */
  @Getter
  private final JunctionId outgoingJunctionId;

  /**
   * Sign at the end of road
   */
  @Getter
  private final Sign outSign;

  /**
   * Length of road in meters
   */
  @Getter
  private final double length;

  /**
   * Set for cars incoming onto this road
   */
  @Builder.Default
  private Set<CarEditable> incomingCars = new ConcurrentSkipListSet<>();

  @Override
  public Optional<CarReadable> getCarInFrontReadable(CarReadable car) {
    return cars.stream()
        .filter(otherCar -> otherCar.getPositionOnRoad() > car.getPositionOnRoad())
        .findFirst()
        .map(nextCar -> nextCar);
  }

  @Override
  public Optional<CarReadable> getCarBeforePosition(double position) {
    return streamCarsFromExitReadable().filter(car -> car.getPositionOnRoad() < position).findFirst();
  }

  @Override
  public Optional<CarReadable> getCarAtEntryReadable() {
    return Optional.ofNullable(cars.peekFirst());
  }

  @Override
  public Optional<CarReadable> getCarAtExitReadable() {
    return Optional.ofNullable(cars.peekLast());
  }

  @Override
  public Stream<CarReadable> streamCarsFromEntryReadable() {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(cars.iterator(), 0), false);
  }

  @Override
  public Stream<CarReadable> streamCarsFromExitReadable() {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(cars.descendingIterator(), 0), false);
  }

  @Override
  public int numberOfCars() {
    return cars.size();
  }

  @Override
  public void addIncomingCar(CarEditable car) {
    incomingCars.add(car);
    //TODO validate if cars is added properly to set (look before this commit version) - future work
  }

  @Override
  public void addCarAtEntry(CarEditable car) {
    if(!cars.isEmpty()){
      CarReadable firstCarOnRoad = cars.peekFirst();
      if(firstCarOnRoad.getPositionOnRoad() < car.getPositionOnRoad()){
        log.debug("Road: " + roadId + " Try to add car at entry with higher position than first one car on road, car: "
              + car.getCarId() + ", position: " + car.getPositionOnRoad() + ", speed: " + car.getSpeed() + ", first car: " + firstCarOnRoad.getCarId()
              + ", position: " + firstCarOnRoad.getPositionOnRoad() + ", speed: " + firstCarOnRoad.getSpeed() + ". Collision after crossroad!");

        //Move back car to be before car he hit after collision
        car.setPositionOnRoadAndSpeed(firstCarOnRoad.getPositionOnRoad()
            - Math.min(0.1, firstCarOnRoad.getPositionOnRoad() * 0.1), firstCarOnRoad.getSpeed() * 0.9);
      }
    }
    cars.addFirst(car);
  }

  @Override
  public void addNewCar(CarEditable car){
    if(!cars.isEmpty()){
      double position = cars.getFirst().getPositionOnRoad() - cars.getFirst().getLength() - 0.3;
      double speed = cars.getFirst().getSpeed();

      if(position > length){
        car.setPositionOnRoadAndSpeed(0, 10);
      }

      for (final CarEditable c : cars) {
        double start = c.getPositionOnRoad();
        double end = start + c.getLength();

        if(position >= start && position <= end){
          position = end + 0.3;
        } else{
          break;
        }
      }

        car.setPositionOnRoadAndSpeed(position, speed);
    }
    cars.addFirst(car);
  }

  @Override
  public Optional<CarEditable> pollCarAtExit() {
    return Optional.ofNullable(cars.pollLast());
  }

  @Override
  public Optional<CarEditable> getCarAtExit() {
    return Optional.ofNullable(cars.peekLast());
  }

  @Override
  public Stream<CarEditable> pollIncomingCars() {
    Set<CarEditable> oldIncomingCars = incomingCars;
    this.incomingCars = new HashSet<>();
    return oldIncomingCars.stream();
  }

  @Override
  public Stream<CarEditable> streamCarsFromEntryEditable() {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(cars.iterator(), 0), false);
  }

  @Override
  public Stream<CarEditable> streamCarsFromExitEditable() {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(cars.descendingIterator(), 0), false);
  }

  public boolean removeCar(CarEditable car) {
    return this.cars.remove(car);
  }
}

