package model.follow;

import model.car.CarData;
import model.car.CarEnvironment;

public class IDMDecider implements IDecider{
    IFollowingModel followingModel;

    public IDMDecider(){
        this.followingModel = new IDM();
    }

    public IDMDecider(IDM followingModel){
        this.followingModel = followingModel;
    }

    @Override
    public double makeDecision(CarEnvironment environment) {    //Unpack environment and call IDM calculation
        CarData managedCar = environment.getManagedCar();
        CarData carAhead = environment.getCarAhead();

        return followingModel.calculateAcceleration(managedCar, carAhead);
    }
}
