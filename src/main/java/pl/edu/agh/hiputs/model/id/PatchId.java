package pl.edu.agh.hiputs.model.id;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.utils.uuid.UUIDProvider;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class PatchId {

  private final String value;

  public static PatchId random() {
    return new PatchId(UUIDProvider.nextUUID().toString());
  }
}
