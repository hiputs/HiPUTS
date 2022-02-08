package pl.edu.agh.follow;

import org.junit.jupiter.api.Test;
import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.car.CarEnvironment;
import pl.edu.agh.model.car.CarReadOnly;
import pl.edu.agh.model.follow.IDM;
import pl.edu.agh.model.follow.IDMDecider;
import pl.edu.agh.model.follow.IDMecider;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IDMDeciderTest {

    private CarReadOnly createCar(double position, double speed, double length, double maxSpeed) {
        Car managedCar = new Car(length, maxSpeed);
        managedCar.setPosition(position);
        managedCar.setSpeed(speed);
        return managedCar;
    }

    @Test
    void makeDecision_toCloseStopped() {
        double distanceHeadway = 2.0;
        double timeHeadway = 2.0;
        double maxAcceleration = 2.0;
        double maxDeceleration = 3.5;
        double length = 5.0;
        double maxSpeed = 20.0;
        IDM model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarReadOnly managedCar = createCar(10, 0, length, maxSpeed);
        CarReadOnly aheadCar = createCar(10 + length + distanceHeadway, 0, length, maxSpeed);
        IDMecider decider = new IDMDecider(model);
        CarEnvironment environment = new CarEnvironment(managedCar, aheadCar);
        double acceleration = decider.makeDecision(environment);
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
        IDM model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarReadOnly managedCar = createCar(10, 0, length, maxSpeed);
        CarReadOnly aheadCar = createCar(517, maxSpeed, length, maxSpeed);
        IDMecider decider = new IDMDecider(model);
        CarEnvironment environment = new CarEnvironment(managedCar, aheadCar);
        double acceleration = decider.makeDecision(environment);
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
        IDM model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarReadOnly managedCar = createCar(10, maxSpeed, length, maxSpeed);
        CarReadOnly aheadCar = createCar(5017, maxSpeed, length, maxSpeed);
        IDMecider decider = new IDMDecider(model);
        CarEnvironment environment = new CarEnvironment(managedCar, aheadCar);
        double acceleration = decider.makeDecision(environment);
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
        IDM model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarReadOnly managedCar = createCar(10, maxSpeed, length, maxSpeed);
        CarReadOnly aheadCar = createCar(10  + length + distanceHeadway, maxSpeed, length, maxSpeed);
        IDMecider decider = new IDMDecider(model);
        CarEnvironment environment = new CarEnvironment(managedCar, aheadCar);
        double acceleration = decider.makeDecision(environment);
        double res = acceleration;  //acceleration is lover than  (-lowestDeceleration)
        assertTrue(res <= 0.001 - maxDeceleration, "Result was: " + res + ", but should be lower than: " + (-maxDeceleration));
    }

    @Test
    void makeDecision_breakSafety() {
        double distanceHeadway = 2.0;
        double timeHeadway = 2.0;
        double maxAcceleration = 2.0;
        double maxDeceleration = 3.5;
        double length = 5.0;
        double maxSpeed = 20.0;
        IDM model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarReadOnly managedCar = createCar(10, maxSpeed, length, maxSpeed);
        CarReadOnly aheadCar = createCar(10 + length + distanceHeadway + maxSpeed * timeHeadway, maxSpeed, length, maxSpeed);
        IDMecider decider = new IDMDecider(model);
        CarEnvironment environment = new CarEnvironment(managedCar, aheadCar);
        double acceleration = decider.makeDecision(environment);
        double res = Math.abs(acceleration + maxAcceleration);
        assertTrue(res <= 0.001, "Result was: " + res + ", but should be lower than 0");
    }
}