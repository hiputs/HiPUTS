package pl.edu.agh.model.follow;

import pl.edu.agh.model.car.CarReadOnly;

public interface IFollowingModel {
    double calculateAcceleration(final CarReadOnly managedCar, final CarReadOnly aheadCar) ;
}
