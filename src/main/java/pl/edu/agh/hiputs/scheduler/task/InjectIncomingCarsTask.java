package pl.edu.agh.hiputs.scheduler.task;

import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.communication.model.serializable.SCar;
import pl.edu.agh.hiputs.model.actor.MapFragment;
import pl.edu.agh.hiputs.model.car.Car;

import java.util.List;

@RequiredArgsConstructor
public class InjectIncomingCarsTask implements Runnable {

    private final List<SCar> sCars;
    private final MapFragment mapFragment;

    @Override
    public void run() {
        sCars
                .parallelStream()
                .forEach(sCar -> {
                    Car car = sCar.toRealObject();
                    mapFragment.insertCar(car);
                });
    }
}
