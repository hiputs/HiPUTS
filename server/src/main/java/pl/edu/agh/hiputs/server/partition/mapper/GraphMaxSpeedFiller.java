package pl.edu.agh.hiputs.server.partition.mapper;

import java.util.Map;
import pl.edu.agh.hiputs.server.partition.model.JunctionData;
import pl.edu.agh.hiputs.server.partition.model.WayData;
import pl.edu.agh.hiputs.server.partition.model.graph.Edge;
import pl.edu.agh.hiputs.server.partition.model.graph.Graph;

class GraphMaxSpeedFiller implements GraphTransformer {

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
      return Integer.parseInt(tags.get(MAX_SPEED_TAG));
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
