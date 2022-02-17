package pl.edu.agh.model.follow;

import pl.edu.agh.model.car.CarEnvironment;
import pl.edu.agh.model.car.CarReadOnly;

public interface IDecider {
    double makeDecision(CarReadOnly managedCar, CarEnvironment environment);
}
