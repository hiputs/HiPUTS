package pl.edu.agh.hiputs.follow;

import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarRead;
import pl.edu.agh.hiputs.model.follow.IDM;
import pl.edu.agh.hiputs.model.follow.IFollowingModel;
import pl.edu.agh.hiputs.model.id.CarId;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IDMTest {

    private CarRead createCar(double position, double speed, double length, double maxSpeed) {
        return Car.builder()
                .id(new CarId())
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
        IFollowingModel model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarRead managedCar = createCar(10, 0, length, maxSpeed);
        CarRead aheadCar = createCar(10 + length + distanceHeadway, 0, length, maxSpeed);
        double acceleration = model.calculateAcceleration(
                managedCar.getSpeed(), managedCar.getMaxSpeed(),
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
        IFollowingModel model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarRead managedCar = createCar(10, 0, length, maxSpeed);
        CarRead aheadCar = createCar(517, maxSpeed, length, maxSpeed);
        double acceleration = model.calculateAcceleration(
                managedCar.getSpeed(), managedCar.getMaxSpeed(),
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
        IFollowingModel model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarRead managedCar = createCar(10, maxSpeed, length, maxSpeed);
        CarRead aheadCar = createCar(5017, maxSpeed, length, maxSpeed);
        double acceleration = model.calculateAcceleration(
                managedCar.getSpeed(), managedCar.getMaxSpeed(),
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
        IFollowingModel model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarRead managedCar = createCar(10, maxSpeed, length, maxSpeed);
        CarRead aheadCar = createCar(10 + length + distanceHeadway, maxSpeed, length, maxSpeed);
        double acceleration = model.calculateAcceleration(
                managedCar.getSpeed(), managedCar.getMaxSpeed(),
                aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane(),
                managedCar.getSpeed() - aheadCar.getSpeed());
        double res = acceleration;  //acceleration is lover than  (-lowestDeceleration)
        assertTrue(res <= 0.001 - maxDeceleration,
                "Result was: " + res + ", but should be lower than: " + (-maxDeceleration)
        );
    }

    @Test
    void calculateAcceleration_breakSafety() {
        double distanceHeadway = 2.0;
        double timeHeadway = 2.0;
        double maxAcceleration = 2.0;
        double maxDeceleration = 3.5;
        double length = 5.0;
        double maxSpeed = 20.0;
        IFollowingModel model = new IDM(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
        CarRead managedCar = createCar(10, maxSpeed, length, maxSpeed);
        double aheadCarPosition = 10 + length + distanceHeadway + maxSpeed * timeHeadway;
        CarRead aheadCar = createCar(aheadCarPosition, maxSpeed, length, maxSpeed);
        double acceleration = model.calculateAcceleration(
                managedCar.getSpeed(), managedCar.getMaxSpeed(),
                aheadCar.getPositionOnLane() - aheadCar.getLength() - managedCar.getPositionOnLane(),
                managedCar.getSpeed() - aheadCar.getSpeed());
        double res = Math.abs(acceleration + maxAcceleration);
        assertTrue(res <= 0.001, "Result was: " + res + ", but should be lower than 0");
    }
}