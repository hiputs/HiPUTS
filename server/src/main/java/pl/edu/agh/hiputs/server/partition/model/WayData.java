package pl.edu.agh.hiputs.server.partition.model;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.server.partition.model.graph.EdgeData;

@Getter
@Setter
public class WayData implements EdgeData {

  private double length; // wyliczalny atrybut
  private int maxSpeed;
  private boolean isPriorityRoad;
  private boolean isOneWay;

  private Map<String, String> tags;

  private String patchId;

}
