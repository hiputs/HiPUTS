package pl.edu.agh.hiputs.model.map.roadstructure;

import lombok.Data;
import pl.edu.agh.hiputs.model.id.RoadId;

@Data
public class NeighborRoadInfo {
  private final RoadId roadId;
  private final HorizontalSign horizontalSign;

}
