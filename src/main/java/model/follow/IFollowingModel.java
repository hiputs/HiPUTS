package model.follow;

import model.car.CarReadOnly;

public interface IFollowingModel {
    double calculateAcceleration(final CarReadOnly managedCar, final CarReadOnly aheadCar) ;
}
