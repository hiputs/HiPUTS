package pl.edu.agh.partition.model;

import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.partition.model.graph.EdgeData;

@Getter
@Setter
public class PatchConnectionData implements EdgeData {

  private Double throughput;

}
