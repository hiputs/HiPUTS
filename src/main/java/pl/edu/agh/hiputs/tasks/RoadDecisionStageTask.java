package pl.edu.agh.hiputs.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
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

      road.getLanes()
          .stream()
          .map(mapFragment::getLaneEditable)
          .flatMap(LaneEditable::streamCarsFromExitEditable)
          .forEach(car -> {
            LaneId prevLaneId = car.getLaneId();
            car.decide(mapFragment);
            addToIncomingCarsOfDestinationLane(car, prevLaneId, car.getDecision());
          });
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }

  private void addToIncomingCarsOfDestinationLane(CarEditable car, LaneId prevLaneId, Decision decision) {
    if (decision.getRoadId() != null && !prevLaneId.equals(decision.getLaneId())) {
      LaneEditable destinationLane = mapFragment.getLaneEditable(decision.getLaneId());
      destinationLane.addIncomingCar(car);
      log.debug("Car: " + car.getCarId() + ", RoadId: "+decision.getRoadId() + ", addToIncomingCarsOfDestinationLane: " + decision.getLaneId());
    }
  }
}
