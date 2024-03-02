package pl.edu.agh.hiputs.partition.mapper.detector.util.compatibility;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.compatibility.TypeIncompatibility;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
public class OnMotorwaysIncompatibilityFinder implements IncompatibilityFinder {

  private final static String highwayKey = "highway";
  private final static String motorwayValue = "motorway";
  private final static String motorwayLinkValue = "motorway_link";

  @Override
  public List<TypeIncompatibility> lookup(Graph<JunctionData, WayData> graph) {
    return graph.getEdges()
        .values()
        .stream()
        .filter(this::isMotorway)
        .map(this::findIncompatibleNeighbour)
        .flatMap(List::stream)
        .distinct()
        .map(this::edge2TypeCompatibility)
        .toList();
  }

  private boolean isMotorway(Edge<JunctionData, WayData> edge) {
    return edge.getData().getTags().containsKey(highwayKey) && edge.getData()
        .getTags()
        .get(highwayKey)
        .equals(motorwayValue);
  }

  private List<Edge<JunctionData, WayData>> findIncompatibleNeighbour(Edge<JunctionData, WayData> edge) {
    List<Edge<JunctionData, WayData>> candidates = new ArrayList<>();

    if (!edge.getSource().getData().isCrossroad()) {
      candidates.addAll(edge.getSource().getIncomingEdges());
    }
    if (!edge.getTarget().getData().isCrossroad()) {
      candidates.addAll(edge.getTarget().getOutgoingEdges());
    }

    return candidates.stream().filter(this::isNotMotorwayLink).toList();
  }

  private boolean isNotMotorwayLink(Edge<JunctionData, WayData> edge) {
    return !edge.getData().getTags().containsKey(highwayKey) || !(
        edge.getData().getTags().get(highwayKey).equals(motorwayValue) || edge.getData()
            .getTags()
            .get(highwayKey)
            .equals(motorwayLinkValue));
  }

  private TypeIncompatibility edge2TypeCompatibility(Edge<JunctionData, WayData> edge) {
    return new TypeIncompatibility("motorway", "motorway_link", edge);
  }
}
