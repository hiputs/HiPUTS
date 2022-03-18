package pl.edu.agh.model.follow;

import pl.edu.agh.model.car.CarEnvironment;
import pl.edu.agh.model.car.CarRead;

public class IDMDecider implements IDecider {
    IFollowingModel followingModel;

    public IDMDecider() {
        this.followingModel = new IDM();
    }

    public IDMDecider(IDM followingModel) {
        this.followingModel = followingModel;
    }

    @Override
    public double makeDecision(CarRead managedCar, CarEnvironment environment) {    //Unpack environment and call IDM calculation
        double speed = managedCar.getSpeed();
        double desiredSpeed = managedCar.getMaxSpeed();
        double distance = environment.getDistance();
        double deltaSpeed = environment.getPrecedingCar()
                .map(precedingCar -> precedingCar.getSpeed() - managedCar.getSpeed())
                .orElse(0.0);
        return followingModel.calculateAcceleration(speed, desiredSpeed, distance, deltaSpeed);
    }

}
