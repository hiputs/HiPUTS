package pl.edu.agh.hiputs.model.id;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class MapFragmentId {

  String id;

  public static MapFragmentId random() {
    return new MapFragmentId(UUID.randomUUID().toString());
  }

  public static MapFragmentId from(MapFragmentId mapFragmentId) {
    return new MapFragmentId(mapFragmentId.getId());
  }
}
