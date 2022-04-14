package pl.edu.agh.hiputs.model.map.roadstructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.model.id.LaneId;

@Getter
@AllArgsConstructor
public class LaneOnJunction {

  /**
   * Global lane Id
   */
  private final LaneId laneId;

  /**
   * Index of lane on junction
   */
  private final int laneIndexOnJunction;

  /**
   * Direction of lane on junction - either incoming or outgoing
   */
  private final LaneDirection direction;

  /**
   * Status of lane subordination on junction
   */
  private final LaneSubordination subordination;

  /**
   * Light color on lane (green if no traffic lights)
   */
  // TODO: make it possible to change it dynamically - just add @Setter to this field?
  private TrafficLightColor lightColor;
}
