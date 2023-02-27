package pl.edu.agh.hiputs.model.car.driver.deciders.junction;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class JunctionDecision {
  private final double acceleration;
  private Optional<CrossroadDecisionProperties> decisionProperties = Optional.empty();

  public JunctionDecision(double acceleration, CrossroadDecisionProperties decisionProperties) {
    this.acceleration = acceleration;
    this.decisionProperties = Optional.of(decisionProperties);
  }
}
