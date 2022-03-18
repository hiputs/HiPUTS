package pl.edu.agh.tasks;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import pl.edu.agh.model.actor.MapFragmentReadWrite;
import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.LaneReadWrite;

@RequiredArgsConstructor
public class LaneDecisionStageTask implements Runnable {

    private final MapFragmentReadWrite mapFragment;
    private final LaneId laneId;

    @Override
    public void run() {
        LaneReadWrite lane = mapFragment.getLaneReadWriteById(laneId);

        for (Car car : lane.getCars()) {
            car.decide(mapFragment);
            addToIncomingCarsOfDestinationLane(car, car.getDecision().getLocation().getLane());
        }
    }

    @SneakyThrows
    private void addToIncomingCarsOfDestinationLane(Car car, LaneId destinationLaneId) {
        if (!laneId.equals(destinationLaneId)) {
            LaneReadWrite destinationLane = mapFragment.getLaneReadWriteById(destinationLaneId);
            destinationLane.addToIncomingCars(car);
        }
    }
}
