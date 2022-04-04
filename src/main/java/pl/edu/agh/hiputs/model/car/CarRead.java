package pl.edu.agh.hiputs.model.car;

import pl.edu.agh.hiputs.model.id.CarId;

public interface CarRead {
    double getPosition();

    double getLength();

    double getSpeed();

    double getMaxSpeed();

    CarId getId();
}
