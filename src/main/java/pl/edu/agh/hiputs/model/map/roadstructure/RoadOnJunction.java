package pl.edu.agh.hiputs.model.map.roadstructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.model.id.RoadId;

@Getter
@AllArgsConstructor
public class RoadOnJunction {

  /**
   * Global road Id
   */
  private final RoadId roadId;

  /**
   * Index of road on junction
   */
  private final int roadIndexOnJunction;

  /**
   * Direction of road on junction - either incoming or outgoing
   */
  private final RoadDirection direction;

  /**
   * Status of road subordination on junction
   */
  private final RoadSubordination subordination;
}
