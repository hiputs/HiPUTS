package pl.edu.agh.hiputs.server.partition.model;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.server.partition.model.graph.NodeData;

@Getter
@Builder
public class JunctionData implements NodeData {

  private Double lon;
  private Double lat;
  private Map<String, String> tags;

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
