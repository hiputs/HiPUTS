package model.car;

import lombok.Data;

@Data
public class CarEnvironment {
    CarData managedCar;
    CarData carAhead;
}
