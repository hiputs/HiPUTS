package pl.edu.agh.hiputs.partition.model;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.partition.model.graph.EdgeData;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class WayData implements EdgeData {

  private double length;
  private int maxSpeed;
  private boolean isPriorityRoad;
  @Setter(AccessLevel.NONE)
  private boolean isOneWay;
  @Setter(AccessLevel.NONE)
  private boolean tagsInOppositeMeaning;

  @Setter(AccessLevel.NONE)
  private Map<String, String> tags;

  @Setter(AccessLevel.NONE)
  private List<LaneData> lanes;

  private String patchId;

  public void setPatchId(String patchId) {
    this.patchId = patchId;
  }
}
