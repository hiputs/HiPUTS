package pl.edu.agh.hiputs.tasks;

import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.actor.RoadStructureEditor;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarUpdateResult;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.LaneReadWrite;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

@RequiredArgsConstructor
public class LaneUpdateStageTask implements Runnable {
    private final RoadStructureEditor mapFragment;
    private final LaneId laneId;

    @Override
    public void run() {
        LaneReadWrite lane = mapFragment.getLaneReadWriteById(laneId);
        this.removeLeavingCars(lane);
        this.updateCarsOnLane(lane);
        this.handleIncomingCars(lane);
        lane.clearIncomingCars();
    }

    /**
     * Removes from Lane.carsQueue all cars which decided to leave this lane
     */
    private void removeLeavingCars(LaneReadWrite lane) {
        while (lane.getLastCar()
                .map(car -> !car.getDecision().getLaneId().equals(laneId))
                .orElse(false)
        ) {
            lane.removeLastCar();
        }
    }

    /**
     * Iterates in reverse over Lane.carsQueue and call Car.update()
     *
     * @param lane
     */
    private void updateCarsOnLane(LaneReadWrite lane) {
        List<Car> cars = lane.getCars();
        ListIterator<Car> carsQueueIterator = cars.listIterator(cars.size());
        while (carsQueueIterator.hasPrevious()) {
            Car currentCar = carsQueueIterator.previous();
            CarUpdateResult carUpdateResult = currentCar.update();
        }
    }

    /**
     * Sorts Lane.incomingCars, inserts incoming cars and calls Car.update()
     *
     * @param lane
     */
    private void handleIncomingCars(LaneReadWrite lane) {
        lane.getIncomingCars()
                .stream()
                .sorted(Comparator.<Car>comparingDouble(car ->
                                car.getDecision().getPositionOnLane()
                        ).reversed()
                ).forEach(currentCar -> {
            lane.addFirstCar(currentCar);
            currentCar.update();
        });
    }
}