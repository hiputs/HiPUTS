package pl.edu.agh.hiputs.partition.model;

import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.NodeData;
import pl.edu.agh.hiputs.utils.MinMaxAcc;

@Getter
@Builder
public class PatchData implements NodeData {

  private Graph<JunctionData, WayData> graphInsidePatch;

  @Setter
  private Optional<Double> avgLon;
  @Setter
  private Optional<Double> avgLat;
  @Setter
  private MinMaxAcc minMaxLon;
  @Setter
  private MinMaxAcc minMaxLat;

  @Override
  public void merge(NodeData other) {
    //not implemented
  }

}
