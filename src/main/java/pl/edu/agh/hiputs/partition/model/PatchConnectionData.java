package pl.edu.agh.hiputs.partition.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.edu.agh.hiputs.partition.model.graph.EdgeData;

@Getter
@Builder
@EqualsAndHashCode
public class PatchConnectionData implements EdgeData {

  private Double throughput;

}
