package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.service.complex.ComplexCrossroadsUpdater;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.geom.Point;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@RequiredArgsConstructor
public class IndirectBridgesConnectFixer implements ConnectFixer {
  private final ComplexCrossroadsUpdater complexCrossroadsUpdater;

  @Override
  public Graph<JunctionData, WayData> fixFoundDisconnections(
      List<StronglyConnectedComponent> sCCs,
      List<WeaklyConnectedComponent> wCCs,
      Graph<JunctionData, WayData> graph
  ) {
    if (wCCs.size() > 1) {
      // completing all wcc pairs and retrieving their node representatives with the closest distance
      List<List<WeaklyConnectedComponent>> ccsToConnect = findAllRequiredConnections(wCCs);
      List<List<Node<JunctionData, WayData>>> nodesToConnect = ccsToConnect.stream()
          .map(ccPair -> findClosestNodes(ccPair.get(0), ccPair.get(1), graph))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toList();

      // creating two edges in different directions between all pairs of nodes and adding to graph
      nodesToConnect.stream()
          .map(nodesPair -> createBothEdgesBetweenNodes(nodesPair.get(0), nodesPair.get(1)))
          .flatMap(edgePair -> Stream.of(edgePair.get(0), edgePair.get(1)))
          .forEach(edge -> {
            if (checkAdditionPossibility(edge, graph)) {
              graph.addEdge(edge);
            }
          });

      // triggering update process for earlier found complex crossroads (observer pattern)
      complexCrossroadsUpdater.extendWithNodes(nodesToConnect.stream()
          .flatMap(nodesPair -> Stream.of(nodesPair.get(0), nodesPair.get(1)))
          .collect(Collectors.toSet()));
    }

    return graph;
  }

  private List<List<WeaklyConnectedComponent>> findAllRequiredConnections (List<WeaklyConnectedComponent> wCCs) {
    return IntStream.range(0, wCCs.size() - 1)
        .boxed()
        .map(index -> List.of(wCCs.get(index), wCCs.get(index + 1)))
        .toList();
  }

  private Optional<List<Node<JunctionData, WayData>>> findClosestNodes(
      WeaklyConnectedComponent wCC1, WeaklyConnectedComponent wCC2, Graph<JunctionData, WayData> graph
  ) {
    if (wCC1.getNodesIds().isEmpty() || wCC2.getNodesIds().isEmpty()) {
      return Optional.empty();
    }

    // retrieving average coordinates
    Map<Point, Node<JunctionData, WayData>> wCC1Coordinates = wCC1.getNodesIds().stream()
        .collect(Collectors.toMap(
            nodeId -> Point.convertFromCoords(graph.getNodes().get(nodeId).getData()),
            nodeId -> graph.getNodes().get(nodeId)));
    Map<Point, Node<JunctionData, WayData>> wCC2Coordinates = wCC2.getNodesIds().stream()
        .collect(Collectors.toMap(
            nodeId -> Point.convertFromCoords(graph.getNodes().get(nodeId).getData()),
            nodeId -> graph.getNodes().get(nodeId)));

    OptionalDouble avgX1 = wCC1Coordinates.keySet().stream().mapToDouble(Point::getX).average();
    OptionalDouble avgY1 = wCC1Coordinates.keySet().stream().mapToDouble(Point::getY).average();

    OptionalDouble avgX2 = wCC2Coordinates.keySet().stream().mapToDouble(Point::getX).average();
    OptionalDouble avgY2 = wCC2Coordinates.keySet().stream().mapToDouble(Point::getY).average();

    // finding the closest nodes to average points
    Optional<Point> closestFromWCC2 = Optional.empty();
    if (avgX1.isPresent() && avgY1.isPresent()) {
      closestFromWCC2 = wCC2Coordinates.keySet().stream()
          .min(Comparator.comparingDouble(point ->
              point.distanceTo(new Point(avgX1.getAsDouble(), avgY1.getAsDouble()))));
    }

    Optional<Point> closestFromWCC1 = Optional.empty();
    if (avgX2.isPresent() && avgY2.isPresent()) {
      closestFromWCC1 = wCC1Coordinates.keySet().stream()
          .min(Comparator.comparingDouble(point ->
              point.distanceTo(new Point(avgX2.getAsDouble(), avgY2.getAsDouble()))));
    }

    return closestFromWCC1.isPresent() && closestFromWCC2.isPresent() ?
        Optional.of(List.of(wCC1Coordinates.get(closestFromWCC1.get()), wCC2Coordinates.get(closestFromWCC2.get()))) :
        Optional.empty();
  }

  private List<Edge<JunctionData, WayData>> createBothEdgesBetweenNodes(
      Node<JunctionData, WayData> node1, Node<JunctionData, WayData> node2
  ) {
    Map<String, String> tags = retrieveTagsUsingNodes(node1, node2);
    double length = Point.convertFromCoords(node1.getData()).distanceTo(Point.convertFromCoords(node2.getData()));

    Edge<JunctionData, WayData> edge1 = new Edge<>(
        node1.getId() + "->" + node2.getId(),
        WayData.builder()
            .tags(tags)
            .tagsInOppositeMeaning(false)
            .isOneWay(true)
            .length(length)
            .build());
    Edge<JunctionData, WayData> edge2 = new Edge<>(
        node2.getId() + "->" + node1.getId(),
        WayData.builder()
            .tags(tags)
            .tagsInOppositeMeaning(true)
            .isOneWay(true)
            .length(length)
            .build());

    edge1.setSource(node1);
    edge1.setTarget(node2);
    edge2.setSource(node2);
    edge2.setTarget(node1);

    return List.of(edge1, edge2);
  }

  private Map<String, String> retrieveTagsUsingNodes(
      Node<JunctionData, WayData> node1, Node<JunctionData, WayData> node2
  ) {
    Collection<String> tagsToRetrieve = List.of("highway", "name", "addr:country", "maxspeed");
    Map<String, String> newTags = new HashMap<>();

    Collection<List<String>> joinedTagsFromEdges = Stream.of(
        node1.getIncomingEdges(), node1.getOutgoingEdges(), node2.getIncomingEdges(), node2.getOutgoingEdges())
        .flatMap(List::stream)
        .map(edge -> edge.getData().getTags())
        .flatMap(map -> map.keySet().stream()
            .map(key -> List.of(key, map.get(key))))
        .toList();

    tagsToRetrieve.forEach(tag ->
        getMostOccurredTag(joinedTagsFromEdges, tag)
            .ifPresent(result -> newTags.put(tag, result)));

    return newTags;
  }

  private Optional<String> getMostOccurredTag(Collection<List<String>> tags, String key) {
    return tags.stream()
        .filter(tagEntry -> tagEntry.get(0).equals(key))
        .map(tagEntry -> tagEntry.get(1))
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .max(Comparator.comparingLong(Entry::getValue))
        .map(Entry::getKey);
  }

  private boolean checkAdditionPossibility(Edge<JunctionData, WayData> edge, Graph<JunctionData, WayData> graph) {
    return !graph.getEdges().containsKey(edge.getId()) &&
        graph.getNodes().containsKey(edge.getSource().getId()) &&
        graph.getNodes().containsKey(edge.getTarget().getId());
  }
}
