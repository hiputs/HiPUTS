package pl.edu.agh.hiputs.model.car;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;

@Data
@AllArgsConstructor
public class CarUpdateResult {

  private final RoadId oldRoadId;
  private final LaneId oldLaneId;
  private final RoadId newRoadId;
  private final LaneId newLaneId;
  private final double newPositionOnRoad;

}
