package pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
class MobilModelDecision {
  private final double acceleration;
  private final boolean canChangeLane;
}
