package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.decider;

import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler.TypeOfRoad;

@Service
@Order(1)
public class TypeOfRoadDecider implements Decider {

  private final static String nameKeyInTags = "name";
  private final static String wayTypeKeyInTags = "highway";

  private final static Set<String> highwayLabels = Set.of("motorway", "trunk", "motorway_link", "trunk_link");
  private final static Set<String> ruralLabels =
      Set.of("primary", "secondary", "tertiary", "unclassified", "primary_link", "secondary_link", "tertiary_link");
  private final static Set<String> urbanLabels =
      Set.of("living_street", "service", "bus_guideway", "busway", "escape", "raceway", "road", "residential");

  @Override
  public void decideAboutValue(SpeedResultHandler speedDataHandler) {
    if (speedDataHandler.getEdge().getData().getTags().containsKey(wayTypeKeyInTags)) {
      String wayTypeValue = speedDataHandler.getEdge().getData().getTags().get(wayTypeKeyInTags);
      boolean isNameKeyPresent = speedDataHandler.getEdge().getData().getTags().containsKey(nameKeyInTags);

      if (highwayLabels.contains(wayTypeValue)) {
        speedDataHandler.setTypeOfRoad(TypeOfRoad.Highway);
      } else if ((isNameKeyPresent && ruralLabels.contains(wayTypeValue)) || urbanLabels.contains(wayTypeValue)) {
        speedDataHandler.setTypeOfRoad(TypeOfRoad.Urban);
      } else if (ruralLabels.contains(wayTypeValue)) {
        speedDataHandler.setTypeOfRoad(TypeOfRoad.Rural);
      } else {
        speedDataHandler.setTypeOfRoad(TypeOfRoad.NotClassified);
      }
    } else {
      throw new IllegalArgumentException("Cannot take non-highway type of road.");
    }
  }
}
