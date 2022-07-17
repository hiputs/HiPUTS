package pl.edu.agh.hiputs.communication.model.serializable;

import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.id.LaneId;

public class SDecision implements CustomSerializable<Decision> {

  /**
   * Serialized speed
   */
  private final double speed;

  /**
   * Serialized acceleration
   */
  private final double acceleration;

  /**
   * Serialized laneId
   */
  private final String laneId;

  /**
   * Serialized positionOnLane
   */
  private final double positionOnLane;

  /**
   * Serialized offsetToMoveOnRoute
   */
  private final int offsetToMoveOnRoute;

  public SDecision(Decision readObject) {
    this.speed = readObject.getSpeed();
    this.acceleration = readObject.getAcceleration();
    this.laneId = readObject.getLaneId().getValue();
    this.positionOnLane = readObject.getPositionOnLane();
    this.offsetToMoveOnRoute = readObject.getOffsetToMoveOnRoute();
  }

  @Override
  public Decision toRealObject() {
    return Decision.builder()
        .speed(speed)
        .acceleration(acceleration)
        .laneId(new LaneId(laneId))
        .positionOnLane(positionOnLane)
        .offsetToMoveOnRoute(offsetToMoveOnRoute)
        .build();
  }
}
