package pl.edu.agh.hiputs.model.follow;

import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

public class IdmDecider implements FunctionalDecider {

  private final IFollowingModel followingModel;

  public IdmDecider() {
    this.followingModel = new Idm();
  }

  public IdmDecider(Idm followingModel) {
    this.followingModel = followingModel;
  }

  @Override
  public double makeDecision(CarReadable managedCar,
      CarEnvironment environment, RoadStructureReader roadStructureReader) {    //Unpack environment and call IDM calculation
    double speed = managedCar.getSpeed();
    double desiredSpeed = managedCar.getMaxSpeed();
    double distance = environment.getDistance();
    double deltaSpeed =
        environment.getPrecedingCar().map(precedingCar -> managedCar.getSpeed() - precedingCar.getSpeed()).orElse(0.0);
    return followingModel.calculateAcceleration(speed, desiredSpeed, distance, deltaSpeed);
  }

}
