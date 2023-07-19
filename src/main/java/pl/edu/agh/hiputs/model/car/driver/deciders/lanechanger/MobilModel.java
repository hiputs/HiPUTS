package pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarEnvironmentProvider;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarPrecedingEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.FunctionalDecider;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

@Getter
@RequiredArgsConstructor
public class MobilModel implements ILaneChangeDecider{

  private final FunctionalDecider idm;
  private final CarEnvironmentProvider prospector;
  private static final double ACCELERATION_THRESHOLD = 0.5;
  private static final double BE_SAFE = 2.5;

  @Override
  public boolean makeDecision(CarReadable car, CarPrecedingEnvironment carPrecedingEnvironment, LaneId targetLaneId, RoadStructureReader roadStructureReader) {
  // //  driver acceleration
  //   double driverAcceleration = idm.makeDecision(car, carPrecedingEnvironment,roadStructureReader);
  // //  acceleration for current following car
  //   CarPrecedingEnvironment followingCarEnv = prospector.getFollowingCar(car, roadStructureReader);
  //   double followingDriverAcceleration = idm.makeDecision(followingCarEnv.getThisCar(), followingCarEnv,roadStructureReader);
  //
  //   CarReadable carOnNewLane = Car.builder()
  //       .laneId(targetLaneId)
  //       .positionOnLane(car.getPositionOnLane())
  //       .build();
  //
  // //  driver new acceleration - after lane change
  //   //  driver acceleration
  //   double newDriverAcceleration = idm.makeDecision(car, carPrecedingEnvironment,roadStructureReader);
  //   //  acceleration for current following car
  //   CarPrecedingEnvironment followingCarEnv = prospector.getFollowingCar(car, roadStructureReader);
  //   double newDriverAcceleration = idm.makeDecision(followingCarEnv.getThisCar(), followingCarEnv,roadStructureReader);
    return true;
  }

  // public double calculateDistance() {
  //
  // }
}
