package pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger;

import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MobilModelDecision {
  private final Optional<Double> acceleration;
  private final boolean canChangeLane;
}
