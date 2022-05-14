package pl.edu.agh.hiputs.model.car.deciders.overtaking;

import java.util.Optional;
import lombok.Data;
import pl.edu.agh.hiputs.model.car.CarReadable;

/**
 * Class containing information gathered before overtaking
 */
@Data
public class OvertakingEnvironment {
  final Optional<CarReadable> oppositeCar;
  final Optional<CarReadable> precedingPrecedingCar; // todo different name for this car(?)
  final double distanceOnOppositeLane;
  final double distanceBeforeOvertakenCar;
}
