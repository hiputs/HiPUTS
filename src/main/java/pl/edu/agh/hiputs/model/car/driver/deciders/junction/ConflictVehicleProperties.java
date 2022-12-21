package pl.edu.agh.hiputs.model.car.driver.deciders.junction;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ConflictVehicleProperties {
  private boolean isCrossConflict;
  private double tte_a;
  private double tte_b;
  //private double ttp_z;
  private double ttp_b;
  private CarTrailDeciderData car;
}
