package pl.edu.agh.hiputs.model.id;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureEditor;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class RoadId {

  private final String value;

  public static RoadId random() {
    return new RoadId(UUID.randomUUID().toString());
  }

  public RoadEditable getEditable(RoadStructureEditor editor) {
    return editor.getRoadEditable(this);
  }

  public RoadReadable getReadable(RoadStructureReader reader) {
    return reader.getRoadReadable(this);
  }

  @Override
  public String toString() {
    return "RoadId{" + value + '}';
  }
}
