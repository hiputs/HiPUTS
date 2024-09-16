package pl.edu.agh.hiputs.utils.uuid;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(value = "testMode", havingValue = "false")
public class RandomUUIDGenerator implements UUIDGenerator {

  public UUID nextUUID() {
    return UUID.randomUUID();
  }

}

