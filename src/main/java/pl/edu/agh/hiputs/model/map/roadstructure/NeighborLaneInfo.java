package pl.edu.agh.hiputs.model.map.roadstructure;

import lombok.Data;
import pl.edu.agh.hiputs.model.id.LaneId;

@Data
public class NeighborLaneInfo {
  private final LaneId laneId;
  private final HorizontalSign horizontalSign;

}
