package pl.edu.agh.hiputs.model.id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureEditor;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.utils.uuid.UUIDProvider;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class LaneId {

  private final String value;

  public static LaneId random() {
    return new LaneId(UUIDProvider.nextUUID().toString());
  }

  public LaneEditable getEditable(RoadStructureEditor editor) {
    return editor.getLaneEditable(this);
  }

  public LaneReadable getReadable(RoadStructureReader reader) {
    return reader.getLaneReadable(this);
  }

  @Override
  public String toString() {
    return "LaneId{" + value + '}';
  }
}
