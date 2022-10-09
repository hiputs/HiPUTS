package pl.edu.agh.hiputs.model.car;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import pl.edu.agh.hiputs.model.id.LaneId;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class Decision {

  private final double speed;
  private final double acceleration;
  private final LaneId laneId;
  private final double positionOnLane;
  private final int offsetToMoveOnRoute;

}
