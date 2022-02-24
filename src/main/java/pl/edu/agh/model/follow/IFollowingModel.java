package pl.edu.agh.model.follow;

import pl.edu.agh.model.car.CarReadOnly;

public interface IFollowingModel {
    double calculateAcceleration(double speed, double desiredSpeed, double distance, double deltaSpeed) ;
}
