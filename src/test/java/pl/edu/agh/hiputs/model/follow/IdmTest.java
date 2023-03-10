package pl.edu.agh.hiputs.model.follow;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.IFollowingModel;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.Idm;
import pl.edu.agh.hiputs.model.id.CarId;

class IdmTest {

  private CarReadable createCar(double position, double speed, double length, double maxSpeed) {
    return Car.builder()
        .carId(CarId.random())
        .length(length)
        .maxSpeed(maxSpeed)
        .positionOnLane(position)
        .speed(speed)
        .build();
  }

  @Test
  void calculateAcceleration_toCloseStopped() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    IFollowingModel model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, 0, length, maxSpeed);
    CarReadable aheadCar = createCar(10 + length + distanceHeadway, 0, length, maxSpeed);
    double acceleration = model.calculateAcceleration(managedCar.getSpeed(), managedCar.getMaxSpeed(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane(),
        managedCar.getSpeed() - aheadCar.getSpeed());
    double res = Math.abs(acceleration);
    assertTrue(res <= 0.001, "Result was: " + res + ", but should be close to 0.");
  }

  @Test
  void calculateAcceleration_freeStart() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    IFollowingModel model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, 0, length, maxSpeed);
    CarReadable aheadCar = createCar(517, maxSpeed, length, maxSpeed);
    double acceleration = model.calculateAcceleration(managedCar.getSpeed(), managedCar.getMaxSpeed(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane(),
        managedCar.getSpeed() - aheadCar.getSpeed());
    double res = Math.abs(acceleration - maxAcceleration);
    assertTrue(res <= 0.001, "Result was: " + res + ", but should be close to 0.");
  }

  @Test
  void calculateAcceleration_maxSpeed() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    IFollowingModel model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, maxSpeed, length, maxSpeed);
    CarReadable aheadCar = createCar(5017, maxSpeed, length, maxSpeed);
    double acceleration = model.calculateAcceleration(managedCar.getSpeed(), managedCar.getMaxSpeed(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane(),
        managedCar.getSpeed() - aheadCar.getSpeed());
    double res = Math.abs(acceleration);
    assertTrue(res <= 0.001, "Result was: " + res + ", but should be close to 0.");
  }

  @Test
  void calculateAcceleration_toCloseMoving() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    IFollowingModel model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, maxSpeed, length, maxSpeed);
    CarReadable aheadCar = createCar(10 + length + distanceHeadway, maxSpeed, length, maxSpeed);
    double acceleration = model.calculateAcceleration(managedCar.getSpeed(), managedCar.getMaxSpeed(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane(),
        managedCar.getSpeed() - aheadCar.getSpeed());
    double res = acceleration;  //acceleration is lover than  (-lowestDeceleration)
    assertTrue(res <= 0.001 - maxDeceleration,
        "Result was: " + res + ", but should be lower than: " + (-maxDeceleration));
  }

  @Test
  void calculateAcceleration_breakSafety() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    IFollowingModel model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, maxSpeed, length, maxSpeed);
    double aheadCarPosition = 10 + length + distanceHeadway + maxSpeed * timeHeadway;
    CarReadable aheadCar = createCar(aheadCarPosition, maxSpeed, length, maxSpeed);
    double acceleration = model.calculateAcceleration(managedCar.getSpeed(), managedCar.getMaxSpeed(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane(),
        managedCar.getSpeed() - aheadCar.getSpeed());
    double res = Math.abs(acceleration + maxAcceleration);
    assertTrue(res <= 0.001, "Result was: " + res + ", but should be lower than 0");
  }

  @Test
  void calculateAcceleration_breakHard() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    IFollowingModel model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, maxSpeed, length, maxSpeed);
    CarReadable aheadCar = createCar(10 + length + distanceHeadway + maxSpeed * timeHeadway, 0, length, maxSpeed);
    double acceleration = model.calculateAcceleration(
        managedCar.getSpeed(), managedCar.getMaxSpeed(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane(),
        managedCar.getSpeed() - aheadCar.getSpeed());
    assertTrue(acceleration <= 0.001, "Result was: " + acceleration + ", but should be lower than 0");
    assertTrue(acceleration >= -3.5, "Result was: " + acceleration + ", but should be higher than -3.5 (normalDeceleration)");
  }

  @Test
  void calculateAcceleration_minusDistance() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    IFollowingModel model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, maxSpeed, length, maxSpeed);
    CarReadable aheadCar = createCar(1, 0, length, maxSpeed);
    double acceleration = model.calculateAcceleration(
        managedCar.getSpeed(), managedCar.getMaxSpeed(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane(),
        managedCar.getSpeed() - aheadCar.getSpeed());
    assertTrue(acceleration <= 0.001, "Result was: " + acceleration + ", but should be lower than 0");
    assertTrue(acceleration >= -3.5, "Result was: " + acceleration + ", but should be higher than -3.5 (normalDeceleration)");
  }
}