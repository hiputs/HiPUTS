package pl.edu.agh.hiputs.model.car.driver.deciders.overtaking;

import java.util.Optional;
import lombok.Data;
import pl.edu.agh.hiputs.model.car.CarReadable;

/**
 * Class containing information gathered before overtaking </br>
 * distance is calculated respectively to appropriate car
 * or to place where car can no longer overtake (crossroad or lane where horizontal sign not allow for overtaking)
 */
@Data
public class OvertakingEnvironment {
  final Optional<CarReadable> oppositeCar;
  final Optional<CarReadable> carBeforeOvertakenCar;
  final double distanceOnOppositeLane;
  final double distanceBeforeOvertakenCar;
}
