package pl.edu.agh.hiputs.tasks;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureEditor;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

@Slf4j
@RequiredArgsConstructor
public class LaneUpdateStageTask implements Runnable {

  private final RoadStructureEditor mapFragment;
  private final LaneId laneId;

  @Override
  public void run() {
    try {
      LaneEditable lane = mapFragment.getLaneEditable(laneId);
      this.removeLeavingCars(lane);
      this.updateCarsOnLane(lane);
      this.handleIncomingCars(lane);
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }

  /**
   * Removes from Lane.carsQueue all cars which decided to leave this lane
   */
  private void removeLeavingCars(LaneEditable lane) {
    while (lane
            .getCarAtExit()
            .map(car -> Objects.isNull(laneId) || !Objects.equals(laneId, car.getDecision().getLaneId()))
            .orElse(false)) {
      CarEditable car = lane.pollCarAtExit().get();
      log.debug("Car: " + car.getCarId() + " with destination lane: " + car.getDecision().getLaneId() + " removeLeavingCar from lane: " + laneId);
    }
  }

  /**
   * Iterates in reverse over Lane.carsQueue and call Car.update()
   *
   * @param lane
   */
  private void updateCarsOnLane(LaneEditable lane) {
    List<CarEditable> carsToRemove = lane
        .streamCarsFromExitEditable()
        .filter(car -> !Objects.equals(car.getDecision().getLaneId(), laneId) || car.update().isEmpty())
        .collect(Collectors.toList());
    for (CarEditable car : carsToRemove) {
      lane.removeCar(car);
          //If remove instance which stay on old lane draw warning
      if(!Objects.equals(car.getDecision().getLaneId(), laneId)){
        //#TODO change log to warning when repair junction decider
        log.debug("TODO: change to warning finally; " + "Car: " + car.getCarId() + " car remove from lane: " + laneId + " due incorrect laneId in decision: " + car.getDecision().getLaneId());
      }
      else{
        log.trace("Car: " + car.getCarId() + " car remove from lane: " + laneId);
      }
    }
  }

  /**
   * Sorts Lane.incomingCars, inserts incoming cars and calls Car.update()
   *
   * @param lane
   */
  private void handleIncomingCars(LaneEditable lane) {
    lane.pollIncomingCars()
        .sorted(Comparator.<CarEditable>comparingDouble(car -> car.getDecision().getPositionOnLane()).reversed())
        .forEach(currentCar -> {
          if (currentCar.update().isPresent()) {
            lane.addCarAtEntry(currentCar);
            log.trace("Car: " + currentCar.getCarId() + " add at entry of lane: " + laneId);
          }
        });
  }
}