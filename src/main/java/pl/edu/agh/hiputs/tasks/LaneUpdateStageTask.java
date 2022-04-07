package pl.edu.agh.hiputs.tasks;

import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.actor.RoadStructureEditor;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.CarUpdateResult;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.LaneEditable;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

@RequiredArgsConstructor
public class LaneUpdateStageTask implements Runnable {
    private final RoadStructureEditor mapFragment;
    private final LaneId laneId;
    
    @Override
    public void run() {
        LaneEditable lane = mapFragment.getLaneEditable(laneId);
        this.removeLeavingCars(lane);
        this.updateCarsOnLane(lane);
        this.handleIncomingCars(lane);
    }
    
    /**
     * Removes from Lane.carsQueue all cars which decided to leave this lane
     */
    private void removeLeavingCars(LaneEditable lane) {
        while (lane.getCarAtExit()
                .map(car -> !car.getDecision().getLaneId().equals(laneId))
                .orElse(false)
        ) {
            lane.pollCarAtExit();
        }
    }
    
    /**
     * Iterates in reverse over Lane.carsQueue and call Car.update()
     *
     * @param lane
     */
    private void updateCarsOnLane(LaneEditable lane) {
        lane.streamCarsFromExitEditable().forEach(CarEditable::update);
    }
    
    /**
     * Sorts Lane.incomingCars, inserts incoming cars and calls Car.update()
     *
     * @param lane
     */
    private void handleIncomingCars(LaneEditable lane) {
        lane.pollIncomingCars().sorted(
                Comparator.<CarEditable>comparingDouble(car -> car.getDecision().getPositionOnLane()).reversed()
        ).forEach(currentCar -> {
            lane.addCarAtEntry(currentCar);
            currentCar.update();
        });
    }
}