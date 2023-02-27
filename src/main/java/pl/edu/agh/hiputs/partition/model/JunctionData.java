package pl.edu.agh.hiputs.partition.model;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.partition.model.graph.NodeData;

@Getter
@Builder
@EqualsAndHashCode
public class JunctionData implements NodeData {

  private Double lon;
  private Double lat;
  private boolean isCrossroad;
  private Map<String, String> tags;

  @Setter
  private String patchId;

  @Override
  public void merge(NodeData other) {
    if (other == null) {
      return;
    }
    if (!(other instanceof JunctionData)) {
      throw new RuntimeException(
          String.format("Cannot merge JunctionData class with other of type %s", other.getClass()));
    }
    merge((JunctionData) other);
  }

  private void merge(JunctionData other)  {
    if ((other.getLon() != null && !other.getLon().equals(this.getLon())) ||
        (other.getLat() != null && !other.getLat().equals(this.getLat()))) {
      throw new RuntimeException("Cannot merge nodes with not compatible coordinates");
    }

    this.isCrossroad = other.isCrossroad || this.isCrossroad;

    if (this.getTags() != null && other.getTags() != null){
      this.tags.putAll(other.getTags());
    }
  }

}
