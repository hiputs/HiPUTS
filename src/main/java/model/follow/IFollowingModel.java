package model.follow;

import model.car.CarData;

public interface IFollowingModel {
    double calculateAcceleration(final CarData managedCar, final CarData aheadCar) ;
}
