package pl.edu.agh.hiputs.partition.model;

import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.NodeData;

@Getter
@Builder
public class PatchData implements NodeData {

  private Graph<JunctionData, WayData> graphInsidePatch;

  @Setter
  private Optional<Double> avgLon;
  @Setter
  private Optional<Double> avgLat;

  @Override
  public void merge(NodeData other) {
    //not implemented
  }

}
