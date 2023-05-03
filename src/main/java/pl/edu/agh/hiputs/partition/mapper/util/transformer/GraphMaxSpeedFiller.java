package pl.edu.agh.hiputs.partition.mapper.util.transformer;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.WayData;

@Slf4j
@Service
@Order(2)
public class GraphMaxSpeedFiller implements GraphTransformer {

  private static final String MAX_SPEED_TAG = "maxspeed";

  @Override
  public Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph) {
    for (Edge<JunctionData, WayData> edge : graph.getEdges().values()) {
      int maxSpeed = calculateMaxSpeed(edge.getData().getTags());
      edge.getData().setMaxSpeed(maxSpeed);
    }
    return graph;
  }

  private int calculateMaxSpeed(Map<String, String> tags) {
    if (tags.containsKey(MAX_SPEED_TAG)) {
      try {
        int res = Integer.parseInt(tags.get(MAX_SPEED_TAG));
      } catch (NumberFormatException e) {
        log.warn(e.getMessage());
      }
    }

    if (tags.containsKey("highway")) {
      switch (tags.get("highway")) {
        case "motorway":
          return 140;
        case "trunk":
          return 90;
        case "living_street":
          return 20;
      }
    }

    //todo should be 50 or 90 - it is hard to parse whether edge is in city area or not :(
    return 50;
  }
}
