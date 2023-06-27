package pl.edu.agh.hiputs.partition.mapper.helper.service.oneway;

import java.util.Map;

public interface OneWayProcessor {

  boolean checkFromTags(Map<String, String> tags);

}
