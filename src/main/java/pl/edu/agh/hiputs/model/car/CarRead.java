package pl.edu.agh.hiputs.model.car;

import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.LaneId;

public interface CarRead {
    double getPositionOnLane();

    LaneId getLaneId();

    double getLength();

    double getSpeed();

    double getMaxSpeed();

    CarId getId();
}
