package pl.edu.agh.hiputs.partition.mapper.util.oneway;

import java.util.Map;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class OldIncorrectOneWayProcessor implements OneWayProcessor{
  private final static String ONEWAY_KEY = "oneway";
  private final static String ONEWAY_TRUE_VALUE = "true";

  @Override
  public boolean checkFromTags(Map<String, String> tags) {
    return tags.containsKey(ONEWAY_KEY) && tags.get(ONEWAY_KEY).equals(ONEWAY_TRUE_VALUE);
  }
}
