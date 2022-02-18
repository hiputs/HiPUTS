package pl.edu.agh.model.follow;

import pl.edu.agh.model.car.CarEnvironment;
import pl.edu.agh.model.car.CarRead;

public interface IDecider {

    double makeDecision(CarRead managedCar, CarEnvironment environment);

}
