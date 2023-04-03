package pl.edu.agh.hiputs.partition.mapper.util.oneway;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class StandardOsmAndRoundaboutOnewayProcessor implements OneWayProcessor{

  private final static String ONEWAY_KEY = "oneway";
  private final static String ONEWAY_TRUE_VALUE = "yes";
  private final static String JUNCTION_KEY = "junction";
  private final static String JUNCTION_ROUNDABOUT_VALUE = "roundabout";

  @Override
  public boolean checkFromTags(Map<String, String> tags) {
    return tags.containsKey(ONEWAY_KEY) && tags.get(ONEWAY_KEY).equals(ONEWAY_TRUE_VALUE)
        || tags.containsKey(JUNCTION_KEY) && tags.get(JUNCTION_KEY).equals(JUNCTION_ROUNDABOUT_VALUE);
  }
}
