package pl.edu.agh.model.car;

import pl.edu.agh.model.id.CarId;

public interface CarRead {
    double getPosition();

    double getLength();

    double getSpeed();

    double getMaxSpeed();

    CarId getId();
}
