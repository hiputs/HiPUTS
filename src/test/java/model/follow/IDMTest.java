package model.follow;

import model.car.CarData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IDMTest {

    @Test
    void calculateAcceleration_toClose() {
        double distanceHeadway = 2.0;
        double timeHeadway = 2.0;
        double maxAcceleration = 0.3;
        double maxDeceleration = 0.3;
        double length = 5.0;
        double maxSpeed = 20.0;
        IFollowingModel model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarData managedCar = new CarData(10, 0, length, maxSpeed);
        CarData aheadCar = new CarData(17, 0, length, maxSpeed);
        double acceleration = model.calculateAcceleration(managedCar, aheadCar);
        double res = Math.abs(acceleration);
        assertTrue(res <= 0.001, "Result was: " + res + ", but should be close to 0.");
    }

    @Test
    void calculateAcceleration_freeStart() {
        double distanceHeadway = 2.0;
        double timeHeadway = 2.0;
        double maxAcceleration = 0.3;
        double maxDeceleration = 0.3;
        double length = 5.0;
        double maxSpeed = 20.0;
        IFollowingModel model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarData managedCar = new CarData(10, 0, length, maxSpeed);
        CarData aheadCar = new CarData(517, maxSpeed, length, maxSpeed);
        double acceleration = model.calculateAcceleration(managedCar, aheadCar);
        double res = Math.abs(acceleration - maxAcceleration);
        assertTrue(res <= 0.001, "Result was: " + res + ", but should be close to 0.");
    }

    @Test
    void calculateAcceleration_maxSpeed() {
        double distanceHeadway = 2.0;
        double timeHeadway = 2.0;
        double maxAcceleration = 0.3;
        double maxDeceleration = 0.3;
        double length = 5.0;
        double maxSpeed = 20.0;
        IFollowingModel model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarData managedCar = new CarData(10, maxSpeed, length, maxSpeed);
        CarData aheadCar = new CarData(5017, maxSpeed, length, maxSpeed);
        double acceleration = model.calculateAcceleration(managedCar, aheadCar);
        double res = Math.abs(acceleration);
        assertTrue(res <= 0.001, "Result was: " + res + ", but should be close to 0.");
    }

    @Test
    void calculateAcceleration_toCloseMoving() {
        double distanceHeadway = 2.0;
        double timeHeadway = 2.0;
        double maxAcceleration = 0.3;
        double maxDeceleration = 0.3;
        double length = 5.0;
        double maxSpeed = 20.0;
        IFollowingModel model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarData managedCar = new CarData(10, maxSpeed, length, maxSpeed);
        CarData aheadCar = new CarData(18, maxSpeed, length, maxSpeed);
        double acceleration = model.calculateAcceleration(managedCar, aheadCar);
        double res = acceleration;  //acceleration is lover than  (-lowestDeceleration)
        assertTrue(res <= 0.001 - maxDeceleration, "Result was: " + res + ", but should be lower than: " + (-maxDeceleration));
    }
}