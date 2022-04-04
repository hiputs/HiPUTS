package pl.edu.agh.hiputs.model.follow;

import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarRead;

public interface IDecider {

    double makeDecision(CarRead managedCar, CarEnvironment environment);

}
