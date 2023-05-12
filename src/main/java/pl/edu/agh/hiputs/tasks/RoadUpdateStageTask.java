package pl.edu.agh.hiputs.tasks;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureEditor;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;

@Slf4j
@RequiredArgsConstructor
public class RoadUpdateStageTask implements Runnable {

  private final RoadStructureEditor mapFragment;
  private final RoadId roadId;

  @Override
  public void run() {
    try {
      RoadEditable road = mapFragment.getRoadEditable(roadId);
      this.removeLeavingCars(road);
      this.updateCarsOnRoad(road);
      this.handleIncomingCars(road);
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }

  /**
   * Removes from Road.carsQueue all cars which decided to leave this road
   */
  private void removeLeavingCars(RoadEditable road) {
    while (road
            .getCarAtExit()
            .map(car -> Objects.isNull(roadId) || !Objects.equals(roadId, car.getDecision().getRoadId()))
            .orElse(false)) {
      CarEditable car = road.pollCarAtExit().get();
      log.debug("Car: " + car.getCarId() + " with destination road: " + car.getDecision().getRoadId() + " removeLeavingCar from road: " + roadId);
    }
  }

  /**
   * Iterates in reverse over Road.carsQueue and call Car.update()
   *
   * @param road
   */
  private void updateCarsOnRoad(RoadEditable road) {
    try {
      List<CarEditable> carsToRemove = road.streamCarsFromExitEditable()
          .filter(car -> !Objects.equals(car.getDecision().getRoadId(), roadId) || car.update().isEmpty())
          .toList();
      for (CarEditable car : carsToRemove) {
        road.removeCar(car);
        //If remove instance which stay on old road draw warning
        if (!Objects.equals(car.getDecision().getRoadId(), roadId)) {
          //#TODO change log to warning when repair junction decider
          // log.trace("Car: " + car.getCarId() + " car remove from road: " + laneId + " due incorrect laneId in decision: " + car.getDecision().getLaneId());
        } else {
          log.warn("Car: " + car.getCarId() + " car remove from road: " + roadId);
        }
      }
    }catch (Exception e) {
       log.warn("Error uptade road - incorrect map error");
      }
  }

  /**
   * Sorts Road.incomingCars, inserts incoming cars and calls Car.update()
   *
   * @param road
   */
  private void handleIncomingCars(RoadEditable road) {
    road.pollIncomingCars()
        .sorted(Comparator.<CarEditable>comparingDouble(car -> car.getDecision().getPositionOnRoad()).reversed())
        .forEach(currentCar -> {
          if (currentCar.update().isPresent()) {
            road.addCarAtEntry(currentCar);
            log.trace("Car: " + currentCar.getCarId() + " add at entry of road: " + roadId);
          }
        });
  }
}