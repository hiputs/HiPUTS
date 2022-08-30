package pl.edu.agh.hiputs.partition.osm.speed.rule.decider;

import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.Map;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.TypeOfRoad;

@Service
@Order(1)
public class TypeOfRoadDecider implements Decider{
  private final static Set<String> highwayLabels = Set.of("motorway", "trunk");

  @Override
  public void decideAboutValue(SpeedResultHandler speedDataHandler) {
    Map<String, String> wayTags = OsmModelUtil.getTagsAsMap(speedDataHandler.getOsmWay());

    if (wayTags.containsKey("highway")) {
      if (highwayLabels.contains(wayTags.get("highway"))) {
        speedDataHandler.setTypeOfRoad(TypeOfRoad.Highway);
      } else if (wayTags.containsKey("name")) {
        speedDataHandler.setTypeOfRoad(TypeOfRoad.Urban);
      } else {
        speedDataHandler.setTypeOfRoad(TypeOfRoad.Rural);
      }
    } else {
      throw new IllegalArgumentException("Cannot take non-highway type of road.");
    }
  }
}
