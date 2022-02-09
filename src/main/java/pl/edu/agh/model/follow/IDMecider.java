package pl.edu.agh.model.follow;

import pl.edu.agh.model.car.CarEnvironment;
import pl.edu.agh.model.car.CarReadOnly;

public interface IDMecider {
    double makeDecision(CarReadOnly car, CarEnvironment environment);
}
