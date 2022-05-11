package pl.edu.agh.hiputs.partition.model;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.NodeData;

@Getter
@Builder
public class PatchData implements NodeData {

  private Graph<JunctionData, WayData> graphInsidePatch;

  @Override
  public void merge(NodeData other) {
    //not implemented
  }

}
