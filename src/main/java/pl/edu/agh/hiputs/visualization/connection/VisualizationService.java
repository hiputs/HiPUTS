package pl.edu.agh.hiputs.visualization.connection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.visualization.connection.producer.CarsProducer;
import proto.model.CarMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisualizationService {

  private final CarsProducer carsProducer;

  private static CarMessage createCarMessage(CarReadable car, LaneReadable lane) {
    double positionOnLane = car.getPositionOnLane() / lane.getLength();
    return CarMessage.newBuilder()
        .setCarId(car.getCarId().getValue())
        .setLength(car.getLength())
        .setAcceleration(car.getAcceleration())
        .setSpeed(car.getSpeed())
        .setMaxSpeed(car.getMaxSpeed())
        .setLaneId(car.getLaneId().getValue())
        .setPositionOnLane(positionOnLane)
        .build();
  }
  public void sendCarsFromMapFragment(MapFragment mapFragment) {
    log.info("Start sending cars from mapFragment:" + mapFragment.getMapFragmentId());

    this.carsProducer.sendCars(
      mapFragment
        .getKnownPatchReadable()
        .stream()
        .flatMap(patch -> patch
            .streamLanesReadable()
            .flatMap(lane -> lane
              .streamCarsFromExitReadable()
              .map(car -> createCarMessage(car, lane))
            )
        )
        .toList(),
      mapFragment
    );
    log.info("End sending cars from mapFragment:" + mapFragment.getMapFragmentId());
  }

}
