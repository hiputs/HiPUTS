package pl.edu.agh.hiputs.utils.uuid;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class UUIDProvider {

  private static UUIDGenerator uuidGenerator = new RandomUUIDGenerator();

  @Autowired
  public void init(UUIDGenerator uuidGenerator) {
    UUIDProvider.uuidGenerator = uuidGenerator;
  }

  public static UUID nextUUID() {
    return uuidGenerator.nextUUID();
  }

}
