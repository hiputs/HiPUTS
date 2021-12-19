package model.car;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class CarEnvironment {
    CarData managedCar;
    CarData carAhead;

    public CarEnvironment() {

    }
}
