package pl.edu.agh.hiputs.model.id;

import java.util.concurrent.ThreadLocalRandom;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Slf4j
public class MapFragmentId {

  private final String id;

  public static MapFragmentId random() {
    return new MapFragmentId("W" + ThreadLocalRandom.current().nextInt(0, 10000));
  }

  public static MapFragmentId from(MapFragmentId mapFragmentId) {
    return new MapFragmentId(mapFragmentId.getId());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !obj.getClass().equals(this.getClass())) {
      return false;
    }
    MapFragmentId objId = (MapFragmentId) obj;

    return objId.getId().equals(this.id);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }
}
