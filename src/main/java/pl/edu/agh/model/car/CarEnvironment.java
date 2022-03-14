package pl.edu.agh.model.car;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;

/**
 * precedingCar contain preceding car if found
 * distance if preceding car is present then indicate distance between cars (includes car length) or distance to
 * nearest crossroad
 */
@Data
@AllArgsConstructor
public class CarEnvironment {
    Optional<CarReadOnly> precedingCar;
    double distance;
}
