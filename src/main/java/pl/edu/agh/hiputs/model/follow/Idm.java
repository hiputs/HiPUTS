package pl.edu.agh.hiputs.model.follow;

public class Idm implements IFollowingModel {

  private final double distanceHeadway;
  private final double timeHeadway;
  private final double normalAcceleration;
  private final double normalDeceleration;

  public Idm() {
    distanceHeadway = 2.0;
    timeHeadway = 2.0;
    normalAcceleration = 2.0;
    normalDeceleration = 3.5;
  }

  public Idm(double distanceHeadway, double timeHeadway, double normalAcceleration, double normalDeceleration) {
    this.distanceHeadway = distanceHeadway;
    this.timeHeadway = timeHeadway;
    this.normalAcceleration = normalAcceleration;
    this.normalDeceleration = normalDeceleration;
  }

  @Override
  public double calculateAcceleration(double speed, double desiredSpeed, double distance, double deltaSpeed) {
    double minimumDistance = distanceHeadway + speed * timeHeadway + ((speed * deltaSpeed) / (2 * Math.sqrt(
        normalAcceleration * normalDeceleration)));
    final double delta = 4;
    return normalAcceleration * (1 - Math.pow(speed / desiredSpeed, delta) - Math.pow(minimumDistance / distance, 2));
  }
}
