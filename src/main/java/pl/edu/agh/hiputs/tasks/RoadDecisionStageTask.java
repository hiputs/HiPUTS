package pl.edu.agh.hiputs.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;

@Slf4j
@RequiredArgsConstructor
public class RoadDecisionStageTask implements Runnable {

  private final MapFragment mapFragment;
  private final RoadId roadId;

  @Override
  public void run() {
    try {
      RoadEditable road = mapFragment.getRoadEditable(roadId);

      road.streamCarsFromExitEditable().forEach(car -> {
        car.decide(mapFragment);
        addToIncomingCarsOfDestinationRoad(car, car.getDecision().getRoadId());
      });
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }

  private void addToIncomingCarsOfDestinationRoad(CarEditable car, RoadId destinationRoadId) {
    if (destinationRoadId != null && !roadId.equals(destinationRoadId)) {
      RoadEditable destinationRoad = mapFragment.getRoadEditable(destinationRoadId);
      destinationRoad.addIncomingCar(car);
      log.debug("Car: " + car.getCarId() + " addToIncomingCarsOfDestinationRoad: " + destinationRoadId);
    }
  }
}
