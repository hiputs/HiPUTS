package pl.edu.agh.hiputs.model.car.driver.deciders.follow;

public interface ICarFollowingModel {

  double calculateAcceleration(double speed, double desiredSpeed, double distance, double deltaSpeed);
}
