package pl.edu.agh.scheduler.task;

import lombok.RequiredArgsConstructor;
import pl.edu.agh.communication.model.serializable.SCar;
import pl.edu.agh.model.actor.MapFragment;
import pl.edu.agh.model.car.Car;

import java.util.List;

@RequiredArgsConstructor
public class InjectIncomingCarsTask implements Runnable {

    private final List<SCar> sCars;
    private final MapFragment mapFragment;

    @Override
    public void run() {
        sCars
                .parallelStream()
                .map(sCar -> {
                    Car car = sCar.toRealObject();
                    mapFragment.insertCar(car);
                    return null;
                });
    }
}
