package pl.edu.agh.hiputs.model.follow;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarReadable;

class IdmDeciderTest {

  private CarReadable createCar(double position, double speed, double length, double maxSpeed) {
    return Car.builder().length(length).maxSpeed(maxSpeed).positionOnLane(position).speed(speed).build();
  }

  @Test
  void makeDecision_toCloseStopped() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    Idm model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, 0, length, maxSpeed);
    CarReadable aheadCar = createCar(10 + length + distanceHeadway, 0, length, maxSpeed);
    FunctionalDecider decider = new IdmDecider(model);
    CarEnvironment environment = new CarEnvironment(Optional.of(aheadCar), Optional.empty(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane());
    double acceleration = decider.makeDecision(managedCar, environment, null);
    double res = Math.abs(acceleration);
    assertTrue(res <= 0.001, "Result was: " + res + ", but should be close to 0.");
  }

  @Test
  void makeDecision_freeStart() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    Idm model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, 0, length, maxSpeed);
    CarReadable aheadCar = createCar(517, maxSpeed, length, maxSpeed);
    FunctionalDecider decider = new IdmDecider(model);
    CarEnvironment environment = new CarEnvironment(Optional.of(aheadCar), Optional.empty(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane());
    double acceleration = decider.makeDecision(managedCar, environment, null);
    double res = Math.abs(acceleration - maxAcceleration);
    assertTrue(res <= 0.001, "Result was: " + res + ", but should be close to 0.");
  }

  @Test
  void makeDecision_maxSpeed() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    Idm model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, maxSpeed, length, maxSpeed);
    CarReadable aheadCar = createCar(5017, maxSpeed, length, maxSpeed);
    FunctionalDecider decider = new IdmDecider(model);
    CarEnvironment environment = new CarEnvironment(Optional.of(aheadCar), Optional.empty(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane());
    double acceleration = decider.makeDecision(managedCar, environment, null);
    double res = Math.abs(acceleration);
    assertTrue(res <= 0.001, "Result was: " + res + ", but should be close to 0.");
  }

  @Test
  void makeDecision_toCloseMoving() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    Idm model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, maxSpeed, length, maxSpeed);
    CarReadable aheadCar = createCar(10 + length + distanceHeadway, maxSpeed, length, maxSpeed);
    FunctionalDecider decider = new IdmDecider(model);
    CarEnvironment environment = new CarEnvironment(Optional.of(aheadCar), Optional.empty(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane());
    double acceleration = decider.makeDecision(managedCar, environment, null);
    double res = acceleration;  //acceleration is lover than  (-lowestDeceleration)
    assertTrue(acceleration <= 0.001 - maxDeceleration,
        "Result was: " + res + ", but should be lower than: " + (-maxDeceleration));
  }

  @Test
  void makeDecision_toCloseMovingManagedCar() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    Idm model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, maxSpeed, length, maxSpeed);
    CarReadable aheadCar = createCar(10 + length + distanceHeadway + maxSpeed * timeHeadway, 0, length, maxSpeed);
    FunctionalDecider decider = new IdmDecider(model);
    CarEnvironment environment = new CarEnvironment(Optional.of(aheadCar), Optional.empty(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane());
    double acceleration = decider.makeDecision(managedCar, environment, null);
    double res = acceleration;  //acceleration is lover than  (-lowestDeceleration)
    assertTrue(acceleration <= 0.001 - maxDeceleration,
        "Result was: " + res + ", but should be lower than: " + (-maxDeceleration));
  }


  @Test
  void makeDecision_toCloseMovingAheadCar() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    Idm model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, 0, length, maxSpeed);
    CarReadable aheadCar = createCar(10 + length + distanceHeadway, maxSpeed, length, maxSpeed);
    FunctionalDecider decider = new IdmDecider(model);
    CarEnvironment environment = new CarEnvironment(Optional.of(aheadCar), Optional.empty(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane());
    double acceleration = decider.makeDecision(managedCar, environment, null);
    double res = acceleration;  //acceleration is lover than  (-lowestDeceleration)
    assertTrue(Math.abs(acceleration) <= 0.001,
        "Result was: " + res + ", but should be close to: " + 0.0);
  }

  @Test
  void makeDecision_toCloseMovingAheadCar_withDistance() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    Idm model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, 0, length, maxSpeed);
    CarReadable aheadCar = createCar(10 + length + distanceHeadway + maxSpeed * timeHeadway, maxSpeed, length, maxSpeed);
    FunctionalDecider decider = new IdmDecider(model);
    CarEnvironment environment = new CarEnvironment(Optional.of(aheadCar), Optional.empty(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane());
    double acceleration = decider.makeDecision(managedCar, environment, null);
    double res = acceleration;  //acceleration is lover than  (-lowestDeceleration)
    assertTrue(acceleration >= 0.0,
        "Result was: " + res + ", but should be higher than: " + 0.0);
  }

  @Test
  void makeDecision_breakSafety() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;
    double length = 5.0;
    double maxSpeed = 20.0;
    Idm model = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    CarReadable managedCar = createCar(10, maxSpeed, length, maxSpeed);
    double aheadCarPosition = 10 + length + distanceHeadway + maxSpeed * timeHeadway;
    CarReadable aheadCar = createCar(aheadCarPosition, maxSpeed, length, maxSpeed);
    FunctionalDecider decider = new IdmDecider(model);
    CarEnvironment environment = new CarEnvironment(Optional.of(aheadCar), Optional.empty(),
        aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane());
    double acceleration = decider.makeDecision(managedCar, environment, null);
    double res = Math.abs(acceleration + maxAcceleration);
    assertTrue(res <= 0.001, "Result was: " + res + ", but should be lower than 0");
  }
}