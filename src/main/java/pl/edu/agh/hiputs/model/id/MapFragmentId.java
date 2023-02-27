package pl.edu.agh.hiputs.model.id;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class MapFragmentId {

  String id;

  public static MapFragmentId random() {
    return new MapFragmentId("W" + ThreadLocalRandom.current().nextInt(0, 10000));
  }

  public static MapFragmentId from(MapFragmentId mapFragmentId) {
    return new MapFragmentId(mapFragmentId.getId());
  }
}
