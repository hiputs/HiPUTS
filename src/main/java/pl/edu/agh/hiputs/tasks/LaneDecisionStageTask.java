package pl.edu.agh.hiputs.tasks;

import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.actor.RoadStructureEditor;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.LaneEditable;

@RequiredArgsConstructor
public class LaneDecisionStageTask implements Runnable {

    private final RoadStructureEditor mapFragment;
    private final LaneId laneId;

    @Override
    public void run() {
        LaneEditable lane = mapFragment.getLaneEditable(laneId);
        lane.streamCarsFromExitEditable().forEach(car -> {
                    car.decide(mapFragment);
                    addToIncomingCarsOfDestinationLane(car, car.getDecision().getLaneId());
                }
        );
    }

    private void addToIncomingCarsOfDestinationLane(CarEditable car, LaneId destinationLaneId) {
        if (!laneId.equals(destinationLaneId)) {
            LaneEditable destinationLane = mapFragment.getLaneEditable(destinationLaneId);
            destinationLane.addIncomingCar(car);
        }
    }
}
