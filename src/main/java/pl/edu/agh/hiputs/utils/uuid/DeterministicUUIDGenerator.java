package pl.edu.agh.hiputs.utils.uuid;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@Component
@ConditionalOnProperty(value = "testMode", havingValue = "true")
public class DeterministicUUIDGenerator implements UUIDGenerator {

  private final static Random random = new Random(0L);

  public UUID nextUUID() {
    var seed = new byte[16];
    random.nextBytes(seed);
    return UUID.nameUUIDFromBytes(seed);
  }
}
