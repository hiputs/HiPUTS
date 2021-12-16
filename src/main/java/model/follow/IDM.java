package model.follow;

import model.car.CarData;

public class IDM implements IFollowingModel{
    private static final double distanceHeadway = 2.0; // #TODO set parameter
    private static final double timeHeadway = 2.0; // #TODO set parameter
    private static final double maxAcceleration = 0.3; // #TODO set parameter
    private static final double maxDeceleration = 0.3; // #TODO set parameter

    @Override
    public double calculateAcceleration(final CarData managedCar, final CarData aheadCar) {
        final double distance = aheadCar.getPosition() - aheadCar.getLength() - managedCar.getPosition();
        final double speed = managedCar.getSpeed();
        final double deltaSpeed = managedCar.getSpeed() - aheadCar.getSpeed();

        double minimumDistance = distanceHeadway + speed * timeHeadway + ((speed * deltaSpeed) / (2 * Math.sqrt(maxAcceleration * maxDeceleration)));

        double desiredSpeed = managedCar.getMaxSpeed();

        final double delta = 4;

        final double acceleration = maxAcceleration * (1 - Math.pow(speed/desiredSpeed, delta) - Math.pow(minimumDistance/distance, 2));

        return acceleration;
    }
}
