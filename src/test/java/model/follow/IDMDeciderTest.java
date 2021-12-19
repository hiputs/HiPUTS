package model.follow;

import model.car.CarData;
import model.car.CarEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IDMDeciderTest {

    @Test
    void makeDecision_toCloseStopped() {
        double distanceHeadway = 2.0;
        double timeHeadway = 2.0;
        double maxAcceleration = 2.0;
        double maxDeceleration = 3.5;
        double length = 5.0;
        double maxSpeed = 20.0;
        IDM model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarData managedCar = new CarData(10, 0, length, maxSpeed);
        CarData aheadCar = new CarData(10 + length + distanceHeadway, 0, length, maxSpeed);
        IDecider decider = new IDMDecider(model);
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
        CarData managedCar = new CarData(10, 0, length, maxSpeed);
        CarData aheadCar = new CarData(517, maxSpeed, length, maxSpeed);
        IDecider decider = new IDMDecider(model);
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
        CarData managedCar = new CarData(10, maxSpeed, length, maxSpeed);
        CarData aheadCar = new CarData(5017, maxSpeed, length, maxSpeed);
        IDecider decider = new IDMDecider(model);
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
        CarData managedCar = new CarData(10, maxSpeed, length, maxSpeed);
        CarData aheadCar = new CarData(10  + length + distanceHeadway, maxSpeed, length, maxSpeed);
        IDecider decider = new IDMDecider(model);
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
        CarData managedCar = new CarData(10, maxSpeed, length, maxSpeed);
        CarData aheadCar = new CarData(10 + length + distanceHeadway + maxSpeed * timeHeadway, maxSpeed, length, maxSpeed);
        IDecider decider = new IDMDecider(model);
        CarEnvironment environment = new CarEnvironment(managedCar, aheadCar);
        double acceleration = decider.makeDecision(environment);
        double res = Math.abs(acceleration + maxAcceleration);
        assertTrue(res <= 0.001, "Result was: " + res + ", but should be lower than 0");
    }
}