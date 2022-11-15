package pl.edu.agh.hiputs.model.id;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureEditor;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class JunctionId {

  private final String value;

  private final JunctionType junctionType;

  private final boolean isOsmNode;

  public JunctionId(String value, JunctionType junctionType) {
    this.value = value;
    this.junctionType = junctionType;
    this.isOsmNode = true;
  }

  public static JunctionId randomCrossroad() {
    return new JunctionId(UUID.randomUUID().toString(), JunctionType.CROSSROAD, false);
  }

  public static JunctionId randomBend() {
    return new JunctionId(UUID.randomUUID().toString(), JunctionType.BEND, false);
  }

  public boolean isCrossroad() {
    return this.junctionType == JunctionType.CROSSROAD;
  }

  public JunctionReadable getReadable(RoadStructureReader reader) {
    return reader.getJunctionReadable(this);
  }

  public JunctionEditable getEditable(RoadStructureEditor editor) {
    return editor.getJunctionEditable(this);
  }

  @Override
  public String toString() {
    return "JunctionId{" + value + " type=" + junctionType + " isOsmNode=" + isOsmNode + '}';
  }
}
