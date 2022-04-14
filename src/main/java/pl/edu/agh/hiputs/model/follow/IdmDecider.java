package pl.edu.agh.hiputs.model.follow;

import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarReadable;

public class IdmDecider implements IDecider {

  final IFollowingModel followingModel;

  public IdmDecider() {
    this.followingModel = new Idm();
  }

  public IdmDecider(Idm followingModel) {
    this.followingModel = followingModel;
  }

  @Override
  public double makeDecision(CarReadable managedCar,
      CarEnvironment environment) {    //Unpack environment and call IDM calculation
    double speed = managedCar.getSpeed();
    double desiredSpeed = managedCar.getMaxSpeed();
    double distance = environment.getDistance();
    double deltaSpeed =
        environment.getPrecedingCar().map(precedingCar -> precedingCar.getSpeed() - managedCar.getSpeed()).orElse(0.0);
    return followingModel.calculateAcceleration(speed, desiredSpeed, distance, deltaSpeed);
  }

}
