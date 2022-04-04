package pl.edu.agh.hiputs.tasks;

import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.actor.MapFragmentReadWrite;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarUpdateResult;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.LaneReadWrite;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@RequiredArgsConstructor
public class LaneUpdateStageTask implements Runnable {
    private final MapFragmentReadWrite mapFragment;
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
     * Counts and removes from Lane.carsQueue all cars which decided to leave this lane
     *
     * @param lane
     */
    private void removeLeavingCars(LaneReadWrite lane) {
        int carsToRemove = 0;
        List<Car> cars = lane.getCars();
        ListIterator<Car> carsQueueIterator = cars.listIterator(cars.size());
        while (carsQueueIterator.hasPrevious()) {
            Car currentCar = carsQueueIterator.previous();
            if (currentCar.getDecision().getLocation().getLane().equals(this.laneId)) {
                break;
            }
            carsToRemove++;
        }
        for (int i = carsToRemove; i > 0; i--) {
            Car car = lane.removeLastCar();
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
        Iterator<Car> incomingCarsIterator = lane
                .getIncomingCars()
                .stream()
                .sorted((car1, car2) -> (int) (
                        car2.getDecision().getLocation().getPositionOnLane()
                                - car1.getDecision().getLocation().getPositionOnLane())
                ).iterator();

        while (incomingCarsIterator.hasNext()) {
            Car currentCar = incomingCarsIterator.next();
            lane.addFirstCar(currentCar);
            CarUpdateResult carUpdateResult = currentCar.update();
        }
    }
}