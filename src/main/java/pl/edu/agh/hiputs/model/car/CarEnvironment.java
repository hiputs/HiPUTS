package pl.edu.agh.hiputs.model.car;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.edu.agh.hiputs.model.id.JunctionId;

/**
 * precedingCar contain preceding car if found
 * nextCrossroadId contain crossroadId if:
 * - precedingCar is empty and searching not exceeds car's route
 * - precedingCar is found on lane ending with crossroad
 * distance contain distance to car (if precedingCar is present, it takes into account length of precedingCar),
 * or to crossroad (if nextCrossroadId is present),
 * or if both Optionals are empty, distance to last lane on car's route
 */
@Data
@AllArgsConstructor
public class CarEnvironment {

  Optional<CarReadable> precedingCar;
  Optional<JunctionId> nextCrossroadId;
  double distance;
}
