package pl.edu.agh.hiputs.partition.mapper.util.oneway;

import java.util.Map;

public interface OneWayProcessor {

  boolean checkFromTags(Map<String, String> tags);

}
