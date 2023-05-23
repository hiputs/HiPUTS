package pl.edu.agh.hiputs.model.car.driver.deciders.lights;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.IFollowingModel;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.JunctionDecision;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.partition.model.lights.LightColor;

@RequiredArgsConstructor
public class RedGreenTrafficLightsDecider implements TrafficLightsDecider{

  private final IFollowingModel idm;

  @Override
  public Optional<JunctionDecision> tryToMakeDecision(CarReadable car, CarEnvironment carEnvironment,
      RoadStructureReader roadStructureReader) {

    return Optional.of(carEnvironment)
        .map(CarEnvironment::getIncomingRoadId)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(roadStructureReader::getRoadReadable)
        .map(RoadReadable::getTrafficIndicator)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(trafficIndicator -> trafficIndicator.getCurrentColor().equals(LightColor.RED))
        .map(trafficIndicator -> new JunctionDecision(idm.calculateAcceleration(
            car.getSpeed(), car.getMaxSpeed(), carEnvironment.getDistance() - car.getLength(), car.getSpeed())
        ));
  }
}
