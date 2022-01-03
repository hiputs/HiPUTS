package pl.edu.agh.model.follow;

import pl.edu.agh.model.car.CarEnvironment;

public interface IDecider {
    double makeDecision(CarEnvironment environment);
}
