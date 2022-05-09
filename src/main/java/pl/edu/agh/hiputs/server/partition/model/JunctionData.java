package pl.edu.agh.hiputs.server.partition.model;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.server.partition.model.graph.NodeData;

@Getter
@Setter
@Builder
public class JunctionData implements NodeData {

  @Setter(AccessLevel.NONE)
  private Double lon;
  @Setter(AccessLevel.NONE)
  private Double lat;
  @Setter(AccessLevel.NONE)
  private Map<String, String> tags;

  private String patchId;

  @Override
  public void merge(NodeData other) {
    if (!(other instanceof JunctionData)) {
      throw new RuntimeException(
          String.format("Cannot merge JunctionData class with other of type %s", other.getClass()));
    }

    if (!((JunctionData) other).getLon().equals(this.getLon()) || !((JunctionData) other).getLat()
        .equals(this.getLat())) {
      throw new RuntimeException("Cannot merge nodes with not compatible coordinates");
    }
  }

}
