package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class StandardTIDeterminer implements TIDeterminer{
  private final static String TI_NODE_KEY = "highway";
  private final static String TI_NODE_VALUE = "traffic_signals";

  @Override
  public boolean checkFromTags(Map<String, String> tags) {
    return tags.containsKey(TI_NODE_KEY) && tags.get(TI_NODE_KEY).equals(TI_NODE_VALUE);
  }
}
