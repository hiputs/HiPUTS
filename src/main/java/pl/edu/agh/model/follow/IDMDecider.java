package pl.edu.agh.model.follow;

import pl.edu.agh.model.car.CarEnvironment;
import pl.edu.agh.model.car.CarReadOnly;

public class IDMDecider implements IDMecider {
    IFollowingModel followingModel;

    public IDMDecider(){
        this.followingModel = new IDM();
    }

    public IDMDecider(IDM followingModel){
        this.followingModel = followingModel;
    }

    @Override
    public double makeDecision(CarReadOnly car, CarEnvironment environment) {    //Unpack environment and call IDM calculation
        double speed = car.getSpeed();
        double desiredSpeed = car.getMaxSpeed();
        double distance = environment.getDistance();
        double deltaSpeed = environment.getPrecedingCar()
                .map(precedingCar -> precedingCar.getSpeed() - car.getSpeed())
                .orElse(0.0);
        return followingModel.calculateAcceleration(speed, desiredSpeed, distance, deltaSpeed);
    }
}
