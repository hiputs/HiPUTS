package pl.edu.agh.hiputs.scheduler.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.example.ExampleCarProvider;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

@Slf4j
@RequiredArgsConstructor
public class CarGenerationTask implements Runnable {

  private final ExampleCarProvider carProvider;
  private final MapFragment mapFragment;
  private final LaneId laneId;
  private final int carsToGenerate;

  @Override
  public void run() {
    try {
      LaneEditable lane = mapFragment.getLaneEditable(laneId);

      for (int i = 0; i < carsToGenerate; i++) {
        Car car = carProvider.generateCar(laneId);
        carProvider.limitSpeedPreventCollisionOnStart(car, lane);
        lane.addNewCar(car);
      }

    } catch (Exception e) {
      log.error("Unexpected exception occurred during initial car generation", e);
    }

  }

}
