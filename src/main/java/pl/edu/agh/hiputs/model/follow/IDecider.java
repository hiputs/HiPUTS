package pl.edu.agh.hiputs.model.follow;

import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarReadable;

public interface IDecider {

    double makeDecision(CarReadable managedCar, CarEnvironment environment);

}
