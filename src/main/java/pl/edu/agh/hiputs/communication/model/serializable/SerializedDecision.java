package pl.edu.agh.hiputs.communication.model.serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;

@Getter
@Builder
@AllArgsConstructor
public class SerializedDecision implements CustomSerializable<Decision> {

  /**
   * Serialized speed
   */
  private final double speed;

  /**
   * Serialized acceleration
   */
  private final double acceleration;

  /**
   * Serialized roadId
   */
  private final String roadId;

  /**
   * Serialized laneId
   */
  private final String laneId;

  /**
   * Serialized positionOnRoad
   */
  private final double positionOnRoad;

  /**
   * Serialized offsetToMoveOnRoute
   */
  private final int offsetToMoveOnRoute;

  /**
   * Serialized crossroadDecisionProperties
   */
  private final SerializedCrossroadDecisionProperties crossroadDecisionProperties;

  public SerializedDecision(Decision readObject) {
    this.speed = readObject.getSpeed();
    this.acceleration = readObject.getAcceleration();
    this.roadId = readObject.getRoadId().getValue();
    this.laneId = readObject.getLaneId().getValue();
    this.positionOnRoad = readObject.getPositionOnRoad();
    this.offsetToMoveOnRoute = readObject.getOffsetToMoveOnRoute();
    this.crossroadDecisionProperties = new SerializedCrossroadDecisionProperties(readObject.getCrossroadDecisionProperties());
  }

  @Override
  public Decision toRealObject() {
    return Decision.builder()
        .speed(speed)
        .acceleration(acceleration)
        .roadId(new RoadId(roadId))
        .laneId(new LaneId(laneId))
        .positionOnRoad(positionOnRoad)
        .offsetToMoveOnRoute(offsetToMoveOnRoute)
        .crossroadDecisionProperties(crossroadDecisionProperties.toRealObject())
        .build();
  }
}
