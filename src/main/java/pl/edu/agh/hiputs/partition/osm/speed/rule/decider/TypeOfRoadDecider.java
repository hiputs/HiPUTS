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
  private final static String nameKeyInTags = "name";
  private final static String highwayKeyInTags = "highway";

  private final static Set<String> highwayLabels = Set.of(
      "motorway", "trunk", "motorway_link", "trunk_link");
  private final static Set<String> ruralLabels = Set.of(
      "primary", "secondary", "tertiary", "unclassified", "primary_link", "secondary_link", "tertiary_link");
  private final static Set<String> urbanLabels = Set.of(
      "living_street", "service", "pedestrian", "bus_guideway", "busway", "escape", "raceway", "road", "residential");

  @Override
  public void decideAboutValue(SpeedResultHandler speedDataHandler) {
    Map<String, String> wayTags = OsmModelUtil.getTagsAsMap(speedDataHandler.getOsmWay());

    if (wayTags.containsKey(highwayKeyInTags)) {
      if (highwayLabels.contains(wayTags.get(highwayKeyInTags))) {
        speedDataHandler.setTypeOfRoad(TypeOfRoad.Highway);
      } else if ((wayTags.containsKey(nameKeyInTags) && ruralLabels.contains(wayTags.get(highwayKeyInTags)))
          || urbanLabels.contains(wayTags.get(highwayKeyInTags))) {
        speedDataHandler.setTypeOfRoad(TypeOfRoad.Urban);
      } else if (ruralLabels.contains(wayTags.get(highwayKeyInTags))){
        speedDataHandler.setTypeOfRoad(TypeOfRoad.Rural);
      } else {
        speedDataHandler.setTypeOfRoad(TypeOfRoad.NotClassified);
      }
    } else {
      throw new IllegalArgumentException("Cannot take non-highway type of road.");
    }
  }
}
