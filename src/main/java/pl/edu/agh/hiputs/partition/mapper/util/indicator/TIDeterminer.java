package pl.edu.agh.hiputs.partition.mapper.util.indicator;

import java.util.Map;

public interface TIDeterminer {

  boolean checkFromTags(Map<String, String> tags);
}
