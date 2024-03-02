package pl.edu.agh.hiputs.partition.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.partition.model.graph.NodeData;
import pl.edu.agh.hiputs.partition.model.lights.control.SignalsControlCenter;
import pl.edu.agh.hiputs.partition.model.relation.Restriction;

@Getter
@Builder
@EqualsAndHashCode
public class JunctionData implements NodeData {
  // constants, need to be set during creation
  private Double lon;
  private Double lat;
  private Map<String, String> tags;

  // can be processed after graph building
  @Setter
  private boolean isCrossroad;
  @Setter
  private String patchId;
  @Setter
  @Builder.Default
  private Optional<SignalsControlCenter> signalsControlCenter = Optional.empty();
  @Builder.Default
  private List<Restriction> restrictions = new ArrayList<>();

  @Builder.Default
  private boolean isOsmNode = true;

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
