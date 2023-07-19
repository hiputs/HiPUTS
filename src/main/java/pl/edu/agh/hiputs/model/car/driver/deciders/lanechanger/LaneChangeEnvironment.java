package pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger;

import lombok.AllArgsConstructor;
import pl.edu.agh.hiputs.model.car.CarReadable;

@AllArgsConstructor
public class LaneChangeEnvironment {
  private final CarReadable precedingCar;
  private final CarReadable followingCar;
  private final CarReadable newPrecedingCar;
  private final CarReadable newFollowingCar;
}
