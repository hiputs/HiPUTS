package model.car;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class CarEnvironment {
    CarReadOnly managedCar;
    CarReadOnly carAhead;

    public CarEnvironment() {

    }
}
