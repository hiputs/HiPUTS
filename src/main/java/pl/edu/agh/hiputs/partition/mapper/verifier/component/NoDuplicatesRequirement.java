package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.lights.control.SignalsControlCenter;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorReadable;

@Service
@Order(5)
public class NoDuplicatesRequirement implements Requirement {

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return noNodesDuplicates(graph) && noEdgesDuplicates(graph) && noLanesDuplicates(graph)
        && noControlCentersDuplicates(graph) && noTrafficIndicatorsDuplicates(graph);
  }

  @Override
  public String getName() {
    return "5. No duplicated objects.";
  }

  private boolean noNodesDuplicates(Graph<JunctionData, WayData> graph) {
    Set<String> nodeIds = graph.getNodes().values().stream().map(Node::getId).collect(Collectors.toSet());
    Set<List<Double>> nodeLocations = graph.getNodes()
        .values()
        .stream()
        .map(node -> List.of(node.getData().getLat(), node.getData().getLon()))
        .collect(Collectors.toSet());

    return graph.getNodes().size() == nodeIds.size() && graph.getNodes().size() == nodeLocations.size();
  }

  private boolean noEdgesDuplicates(Graph<JunctionData, WayData> graph) {
    Set<String> edgeIds = graph.getEdges().values().stream().map(Edge::getId).collect(Collectors.toSet());
    Set<List<String>> edgeLocations = graph.getEdges()
        .values()
        .stream()
        .map(edge -> List.of(edge.getSource().getId(), edge.getTarget().getId()))
        .collect(Collectors.toSet());

    return graph.getEdges().size() == edgeIds.size() && graph.getEdges().size() == edgeLocations.size();
  }

  private boolean noLanesDuplicates(Graph<JunctionData, WayData> graph) {
    Set<String> lanesIds = graph.getEdges()
        .values()
        .stream()
        .flatMap(edge -> edge.getData().getLanes().stream())
        .map(LaneData::getId)
        .collect(Collectors.toSet());

    return graph.getEdges().values().stream().mapToLong(edge -> edge.getData().getLanes().size()).sum()
        == lanesIds.size();
  }

  private boolean noControlCentersDuplicates(Graph<JunctionData, WayData> graph) {
    Set<String> sccIds = graph.getNodes()
        .values()
        .stream()
        .map(node -> node.getData().getSignalsControlCenter())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(SignalsControlCenter::getId)
        .collect(Collectors.toSet());

    return graph.getNodes()
        .values()
        .stream()
        .map(node -> node.getData().getSignalsControlCenter())
        .filter(Optional::isPresent)
        .count() == sccIds.size();
  }

  private boolean noTrafficIndicatorsDuplicates(Graph<JunctionData, WayData> graph) {
    Set<String> tiIds = graph.getEdges()
        .values()
        .stream()
        .map(edge -> edge.getData().getTrafficIndicator())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(TrafficIndicatorReadable::getId)
        .collect(Collectors.toSet());

    return graph.getEdges()
        .values()
        .stream()
        .map(edge -> edge.getData().getTrafficIndicator())
        .filter(Optional::isPresent)
        .count() == tiIds.size();
  }
}
