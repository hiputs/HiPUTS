package pl.edu.agh.hiputs.model.id;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.utils.uuid.UUIDProvider;

@RequiredArgsConstructor
@Getter
// @EqualsAndHashCode
public class PatchId {

  private final String value;

  public static PatchId random() {
    return new PatchId(UUIDProvider.nextUUID().toString());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !obj.getClass().equals(this.getClass())) {
      return false;
    }
    PatchId newPatchId = (PatchId) obj;
    return newPatchId.getValue().equals(this.value);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }
}

