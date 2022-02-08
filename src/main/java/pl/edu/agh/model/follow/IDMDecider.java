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
    public double makeDecision(CarEnvironment environment) {    //Unpack environment and call IDM calculation
        CarReadOnly managedCar = environment.getManagedCar();
        CarReadOnly carAhead = environment.getCarAhead();

        return followingModel.calculateAcceleration(managedCar, carAhead);
    }
}
