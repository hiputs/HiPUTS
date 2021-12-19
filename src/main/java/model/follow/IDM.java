package model.follow;

import model.car.CarData;

public class IDM implements IFollowingModel{
    private final double distanceHeadway; // #TODO set parameter
    private final double timeHeadway; // #TODO set parameter
    private final double maxAcceleration; // #TODO set parameter
    private final double maxDeceleration; // #TODO set parameter

    public IDM(){
        distanceHeadway = 2.0;
        timeHeadway = 2.0;
        maxAcceleration = 0.3;
        maxDeceleration = 0.3;
    }

    public IDM(double distanceHeadway, double timeHeadway, double maxAcceleration, double maxDeceleration){
        this.distanceHeadway = distanceHeadway;
        this.timeHeadway = timeHeadway;
        this.maxAcceleration = maxAcceleration;
        this.maxDeceleration = maxDeceleration;
    }

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
