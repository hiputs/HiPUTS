package pl.edu.agh.hiputs.model.car.driver.deciders.follow;

import pl.edu.agh.hiputs.model.car.driver.DriverParameters;

public class Idm implements IFollowingModel {

  private final double distanceHeadway;
  private final double timeHeadway;
  private final double normalAcceleration;
  private final double normalDeceleration;
  private final int delta;

  public Idm(double distanceHeadway, double timeHeadway, double normalAcceleration, double normalDeceleration, int delta) {
    this.distanceHeadway = distanceHeadway;
    this.timeHeadway = timeHeadway;
    this.normalAcceleration = normalAcceleration;
    this.normalDeceleration = normalDeceleration;
    this.delta = delta;
  }
  public Idm(double distanceHeadway, double timeHeadway, double normalAcceleration, double normalDeceleration) {
    this(distanceHeadway, timeHeadway, normalAcceleration,
        normalDeceleration, 4);
  }

  public Idm(DriverParameters parameters) {
    this(parameters.getIdmDistanceHeadway(), parameters.getIdmTimeHeadway(), parameters.getIdmNormalAcceleration(),
        parameters.getIdmNormalDeceleration(), parameters.getIdmDelta());
  }

  @Override
  public double calculateAcceleration(double speed, double desiredSpeed, double distance, double deltaSpeed) {
    double minimumDistance = distanceHeadway + speed * timeHeadway + ((speed * deltaSpeed) / (2 * Math.sqrt(
        normalAcceleration * normalDeceleration)));
    return Math.max(-normalDeceleration, normalAcceleration * (1 - Math.pow(speed / desiredSpeed, delta) - Math.pow(minimumDistance / distance, 2)));
  }
}
