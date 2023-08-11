package pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger;

import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarFollowingEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarPrecedingEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspector;
import pl.edu.agh.hiputs.model.car.driver.deciders.FunctionalDecider;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

@Getter
@RequiredArgsConstructor
public class MobilModel implements ILaneChangeChecker {

  private final FunctionalDecider idm;
  private final CarProspector prospector;
  private static final double ACCELERATION_THRESHOLD = 0.5;
  private static final double BE_SAFE = 2.5;

  /**
   * acceleration before lane change
   * --------[ a_new_follower  ]------->*---------------------->*-------[   ]------>
   * --------[ a_old_follower  ]------->*------[ a_car ]------->*-------[   ]------>
   *
   * acceleration after lane change
   * --------[ new_a_new_follower  ]------->*---[ new_a_car ]------>*-------[   ]------>
   * --------[ new_a_old_follower  ]------->*---------------------->*-------[   ]------>
   *
   * new_a_car - a_car + politeness_factor * (new_a_new_follower - a_new_follower + new_a_old_follower - a_old_follower) > acceleration_threshold
   * {     driver     }                      {             new follower          } {           old follower            }
   *
   * @param car
   * @param targetLaneId
   * @param roadStructureReader
   * @return boolean if it is possible to change Lane
   */

  @Override
  public MobilModelDecision makeDecision(CarReadable car, LaneId targetLaneId, double politenessFactor,
      RoadStructureReader roadStructureReader) {
    double overallAcceleration,
        carAcceleration,
        carAccelerationLC,
        oldFollowerAcceleration = 0.0,
        oldFollowerAccelerationLC = 0.0,
        newFollowerAcceleration = 0.0,
        newFollowerAccelerationLC = 0.0;


    //  acceleration before lane change
    CarFollowingEnvironment followingCarEnv = prospector.getFollowingCar(car, roadStructureReader);
    CarPrecedingEnvironment carPrecedingEnvironment = prospector.getPrecedingCarOrCrossroad(car, roadStructureReader);

    carAcceleration = idm.makeDecision(car, carPrecedingEnvironment,roadStructureReader);

    if (followingCarEnv.getFollowingCar().isPresent()) {
      oldFollowerAcceleration = idm.makeDecision(followingCarEnv.getFollowingCar().get(),
          new CarPrecedingEnvironment(Optional.of(car),Optional.empty(),followingCarEnv.getDistance()),
          roadStructureReader);
    }

    // new mock car on target lane
    Car car2 = Car.builder()
        .routeWithLocation(car.getCopyOfRoute())
        .laneId(targetLaneId)
        .roadId(car.getRoadId())
        .length(car.getLength())
        .positionOnLane(car.getPositionOnLane())
        .maxSpeed(car.getMaxSpeed())
        .acceleration(car.getAcceleration())
        .acceleration(car.getAcceleration())
        .speed(car.getSpeed())
        .build();

    CarPrecedingEnvironment newPrecedingCarEnv = prospector.getPrecedingCarOrCrossroad(car2, roadStructureReader);

    CarFollowingEnvironment newFollowingCarEnv = prospector.getFollowingCar(car2, roadStructureReader);
    if (newFollowingCarEnv.getFollowingCar().isPresent()) {
      CarReadable newFollower = newFollowingCarEnv.getFollowingCar().get();
      newFollowerAcceleration = idm.makeDecision(newFollower,
          new CarPrecedingEnvironment(newPrecedingCarEnv.getPrecedingCar(), Optional.empty(), newPrecedingCarEnv.getDistance() + newFollowingCarEnv.getDistance()),
          roadStructureReader);
    }

    // acceleration after Lane Change
    carAccelerationLC = idm.makeDecision(car2, newPrecedingCarEnv, roadStructureReader);
    if (newFollowingCarEnv.getFollowingCar().isPresent()) {
      CarReadable newFollower = newFollowingCarEnv.getFollowingCar().get();
      newFollowerAccelerationLC = idm.makeDecision(newFollower,
          new CarPrecedingEnvironment(newPrecedingCarEnv.getPrecedingCar(), Optional.empty(), newFollowingCarEnv.getDistance()),
          roadStructureReader);
    }
    if (followingCarEnv.getFollowingCar().isPresent()) {
      oldFollowerAccelerationLC = idm.makeDecision(followingCarEnv.getFollowingCar().get(),
          new CarPrecedingEnvironment(carPrecedingEnvironment.getPrecedingCar(),Optional.empty(),followingCarEnv.getDistance() + carPrecedingEnvironment.getDistance()),
          roadStructureReader);
    }

    // calculate car environment overall acceleration
    // overallAcceleration = (carAccelerationLC - carAcceleration) + car.getPolitenessFactor() *
    //     ((newFollowerAccelerationLC - newFollowerAcceleration) - (oldFollowerAccelerationLC - oldFollowerAcceleration));
    overallAcceleration = (carAccelerationLC - carAcceleration) + politenessFactor *
        ((newFollowerAccelerationLC - newFollowerAcceleration) - (oldFollowerAccelerationLC - oldFollowerAcceleration));


    return new MobilModelDecision(
        carAccelerationLC,
        (overallAcceleration > ACCELERATION_THRESHOLD) && (newFollowerAccelerationLC > (-1)*BE_SAFE));

  }
}
