package pl.edu.agh.hiputs.tasks;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.CarUpdateResult;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureEditor;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
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
      road.getLanes().stream().map(mapFragment::getLaneEditable).forEach(laneEditable -> {
        // this.removeLeavingCars(laneEditable);
        this.updateCarsOnLane(laneEditable);
        this.handleIncomingCars(laneEditable);
        laneEditable.updateCarSpeedMetrics();
      });
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }

  /**
   * Removes from Lane.carsQueue all cars which decided to leave this lane
   */
  // private void removeLeavingCars(LaneEditable lane) {
  //   while (lane.getCarAtExit()
  //           .map(car -> Objects.isNull(roadId) || !Objects.equals(roadId, car.getDecision().getRoadId()))
  //           .orElse(false)) {
  //     CarEditable car = lane.pollCarAtExit().get();
  //     log.debug("Car: " + car.getCarId() + " with destination road: " + car.getDecision().getRoadId() + "
  //     removeLeavingCar from road: " + roadId);
  //   }
  // }

  /**
   * Iterates in reverse over Lane.carsQueue and call Car.update()
   *
   * @param lane
   */
  private void updateCarsOnLane(LaneEditable lane) {
    try {
      List<CarEditable> carsToRemove = lane.streamCarsFromExitEditable()
          .filter(car -> Objects.isNull(lane.getLaneId()) || !Objects.equals(car.getDecision().getLaneId(),
              lane.getLaneId()) || car.update().isEmpty())
          .toList();
      for (CarEditable car : carsToRemove) {
        lane.removeCar(car);
        log.debug("Car {} was removed.", car.getCarId());
        //If remove instance which stay on old road draw warning
        if (!Objects.equals(car.getDecision().getLaneId(), lane.getLaneId())) {
          //#TODO change log to warning when repair junction decider
          // log.trace("Car: " + car.getCarId() + " car remove from road: " + laneId + " due incorrect laneId in decision: " + car.getDecision().getLaneId());
        } else {
          log.warn(
              "Car: " + car.getCarId() + " car remove from road: " + roadId + " lane: " + lane.getLaneId().getValue());
        }
      }
    }catch (Exception e) {
       log.warn("Error uptade road - incorrect map error");
      }
  }

  /**
   * Sorts Lane.incomingCars, inserts incoming cars and calls Car.update()
   *
   * @param lane
   */
  private void handleIncomingCars(LaneEditable lane) {
    lane.pollIncomingCars()
        .sorted(Comparator.<CarEditable>comparingDouble(car -> car.getDecision().getPositionOnRoad()).reversed())
        .forEach(currentCar -> {
          Optional<CarUpdateResult> carUpdateResult = currentCar.update();
          if (carUpdateResult.isPresent()) {
            CarUpdateResult carUpdate = carUpdateResult.get();
            if (!carUpdate.getNewLaneId().equals(carUpdate.getOldLaneId()) && carUpdate.getNewRoadId()
                .equals(carUpdate.getOldRoadId())) {
              lane.addCarLaneChange(currentCar);
              log.trace(
                  "Car: " + currentCar.getCarId() + " car lane change on the same road: " + roadId + ", prevLane: "
                      + carUpdate.getOldLaneId() + ", newLane: " + carUpdate.getNewLaneId());
            } else {
              lane.addCarAtEntry(currentCar);
              log.trace(
                  "Car: " + currentCar.getCarId() + " add at entry of road: " + roadId + " lane: " + lane.getLaneId()
                      .getValue());
            }
          }
        });
  }
}