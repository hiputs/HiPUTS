package pl.edu.agh.model.follow;

public class IDM implements IFollowingModel{
    private final double distanceHeadway;
    private final double timeHeadway;
    private final double normalAcceleration;
    private final double normalDeceleration;

    public IDM(){
        distanceHeadway = 2.0;
        timeHeadway = 2.0;
        normalAcceleration = 2.0;
        normalDeceleration = 3.5;
    }

    public IDM(double distanceHeadway, double timeHeadway, double normalAcceleration, double normalDeceleration){
        this.distanceHeadway = distanceHeadway;
        this.timeHeadway = timeHeadway;
        this.normalAcceleration = normalAcceleration;
        this.normalDeceleration = normalDeceleration;
    }

    @Override
    public double calculateAcceleration(double speed, double desiredSpeed, double distance, double deltaSpeed) {
        //final double distance = aheadCar.getPosition() - aheadCar.getLength() - managedCar.getPosition();
        //final double speed = managedCar.getSpeed();
        //final double deltaSpeed = managedCar.getSpeed() - aheadCar.getSpeed();

        double minimumDistance = distanceHeadway + speed * timeHeadway + ((speed * deltaSpeed) / (2 * Math.sqrt(normalAcceleration * normalDeceleration)));

        //double desiredSpeed = managedCar.getMaxSpeed();

        final double delta = 4;

        final double acceleration = normalAcceleration * (1 - Math.pow(speed/desiredSpeed, delta) - Math.pow(minimumDistance/distance, 2));

        return acceleration;
    }
}
