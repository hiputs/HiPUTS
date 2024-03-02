package pl.edu.agh.hiputs.partition.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.partition.model.graph.EdgeData;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorReadable;

@Getter
@Builder
@EqualsAndHashCode
public class WayData implements EdgeData {
  // constants, need to be set during creation
  private boolean isOneWay;
  private boolean tagsInOppositeMeaning;
  private Map<String, String> tags;

  // can be initiated before graph building
  @Builder.Default
  private List<LaneData> lanes = new ArrayList<>();

  // can be processed after graph building
  @Setter
  private double length;
  @Setter
  private int maxSpeed;
  @Setter
  private boolean isPriorityRoad;
  @Setter
  private String patchId;
  @Setter
  @Builder.Default
  private Optional<TrafficIndicatorReadable> trafficIndicator = Optional.empty();
}
