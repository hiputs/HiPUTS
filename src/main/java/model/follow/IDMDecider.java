package model.follow;

import model.car.CarEnvironment;
import model.car.CarReadOnly;

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
        CarReadOnly managedCar = environment.getManagedCar();
        CarReadOnly carAhead = environment.getCarAhead();

        return followingModel.calculateAcceleration(managedCar, carAhead);
    }
}
