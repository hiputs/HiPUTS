package model.follow;

import model.car.CarData;
import model.car.CarEnvironment;

public class IDMDecider implements IDecider{
    @Override
    public double makeDecision(CarEnvironment environment) {    //Unpack environment and call IDM calculation
        CarData managedCar = environment.getManagedCar();
        CarData carAhead = environment.getCarAhead();

        IFollowingModel followingModel = new IDM();
        return followingModel.calculateAcceleration(managedCar, carAhead);
    }
}
