package pl.edu.agh.hiputs.partition.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.partition.model.graph.EdgeData;

@Getter
@Builder
@EqualsAndHashCode
public class WayData implements EdgeData {
  // constants, need to be set during creation
  private boolean isOneWay;
  private boolean tagsInOppositeMeaning;
  private Map<String, String> tags;
  private List<LaneData> lanes;

  // can be processed after graph building
  @Setter
  private double length;
  @Setter
  private int maxSpeed;
  @Setter
  private boolean isPriorityRoad;
  @Setter
  private String patchId;
}
