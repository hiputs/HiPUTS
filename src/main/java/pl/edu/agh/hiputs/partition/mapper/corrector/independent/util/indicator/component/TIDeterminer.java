package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component;

import java.util.Map;

public interface TIDeterminer {

  boolean checkFromTags(Map<String, String> tags);
}
