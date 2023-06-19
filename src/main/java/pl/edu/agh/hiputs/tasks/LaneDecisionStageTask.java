package pl.edu.agh.hiputs.tasks;

import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.service.worker.CarGeneratorService;

@Slf4j
@RequiredArgsConstructor
public class LaneDecisionStageTask implements Runnable {

  private final MapFragment mapFragment;
  private final LaneId laneId;
  private final CarGeneratorService carGeneratorService;
  private final boolean isReplaceCarWithFinishedRoute;

  @Override
  public void run() {
    try {
      LaneEditable lane = mapFragment.getLaneEditable(laneId);
      List<CarEditable> carsWithFinishedRoute = new LinkedList<>();

      lane.streamCarsFromExitEditable().forEach(car -> {
        car.decide(mapFragment);
        addToIncomingCarsOfDestinationLane(car, car.getDecision().getLaneId());
        if (car.getDecision().getLaneId() == null) {
          carsWithFinishedRoute.add(car);
        }
      });

      if (isReplaceCarWithFinishedRoute) { // generate new car replacement if needed
        carsWithFinishedRoute.forEach(car -> {
          lane.removeCar(car);
          Car newCar = carGeneratorService.replaceCar(car);

          newCar.decide(mapFragment);
          addToIncomingCarsOfDestinationLane(newCar, newCar.getDecision().getLaneId());
          log.debug("Car {} was replaced", car.getCarId());
        });
      }
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }

  private void addToIncomingCarsOfDestinationLane(CarEditable car, LaneId destinationLaneId) {
    if (destinationLaneId != null && !laneId.equals(destinationLaneId)) {
      LaneEditable destinationLane = mapFragment.getLaneEditable(destinationLaneId);
      destinationLane.addIncomingCar(car);
      log.debug("Car: " + car.getCarId() + " addToIncomingCarsOfDestinationLane: " + destinationLaneId);
    }
  }
}
