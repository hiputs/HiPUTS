package pl.edu.agh.hiputs.model.car;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class CarBasicDeciderData {
  double speed;
  double distance;
  double length;
}
