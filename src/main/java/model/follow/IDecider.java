package model.follow;

import model.car.CarEnvironment;

public interface IDecider {
    double makeDecision(CarEnvironment environment);
}
