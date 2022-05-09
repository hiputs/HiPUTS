package pl.edu.agh.hiputs.server.partition.model;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.server.partition.model.graph.EdgeData;

@Getter
@Setter
@Builder
public class WayData implements EdgeData {

  private double length;
  private int maxSpeed;
  private boolean isPriorityRoad;
  @Setter(AccessLevel.NONE)
  private boolean isOneWay;

  @Setter(AccessLevel.NONE)
  private Map<String, String> tags;

  private String patchId;

  public void setPatchId(String patchId) {
    this.patchId = patchId;
  }
}
