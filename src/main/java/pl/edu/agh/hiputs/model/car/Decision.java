package pl.edu.agh.hiputs.model.car;

import java.util.Optional;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CrossroadDecisionProperties;
import pl.edu.agh.hiputs.model.id.RoadId;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class Decision {

  private final double speed;
  private final double acceleration;
  private final RoadId roadId;
  private final double positionOnRoad;
  private final int offsetToMoveOnRoute;
  private final Optional<CrossroadDecisionProperties> crossroadDecisionProperties;

}
