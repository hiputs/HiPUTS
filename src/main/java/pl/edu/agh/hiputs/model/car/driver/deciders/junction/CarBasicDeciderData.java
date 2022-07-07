package pl.edu.agh.hiputs.model.car.driver.deciders.junction;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class CarBasicDeciderData {
  double speed;
  double distance;
  double length;
}
