package pl.edu.agh.hiputs.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureEditor;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

@Slf4j
@RequiredArgsConstructor
public class LaneDecisionStageTask implements Runnable {

  private final RoadStructureEditor mapFragment;
  private final LaneId laneId;

  @Override
  public void run() {
    try {
      LaneEditable lane = mapFragment.getLaneEditable(laneId);
      lane.streamCarsFromExitEditable().forEach(car -> {
        car.decide(mapFragment);
        addToIncomingCarsOfDestinationLane(car, car.getDecision().getLaneId());
      });
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }

  private void addToIncomingCarsOfDestinationLane(CarEditable car, LaneId destinationLaneId) {
    if (destinationLaneId != null && !laneId.equals(destinationLaneId)) {
      LaneEditable destinationLane = mapFragment.getLaneEditable(destinationLaneId);
      destinationLane.addIncomingCar(car);
    }
  }
}
