package pl.edu.agh.hiputs.service.worker;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.example.ExampleCarProvider;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Service
@RequiredArgsConstructor
public class CarGeneratorService {

  private final MapRepository mapRepository;
  public void generateCars(int count, MapFragment mapFragment){

    List<LaneEditable> lanesEditable = mapFragment.getRandomLanesEditable(count);
    ExampleCarProvider carProvider = new ExampleCarProvider(mapFragment, mapRepository);

    lanesEditable.forEach(lane -> {
      int hops = ThreadLocalRandom.current().nextInt(5, 20);
      Car car = carProvider.generateCar(lane.getLaneId(), hops);
      carProvider.limitSpeedPreventCollisionOnStart(car, lane);
      lane.addNewCar(car);
    });
  }

}
