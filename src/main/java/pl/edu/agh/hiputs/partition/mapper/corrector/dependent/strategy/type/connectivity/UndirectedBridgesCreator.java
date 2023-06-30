package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.geom.Point;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
public class UndirectedBridgesCreator implements BridgesCreator{

  @Override
  public Graph<JunctionData, WayData> createBetweenCCsOnGraph(
      List<StronglyConnectedComponent> sCCs,
      List<WeaklyConnectedComponent> wCCs,
      Graph<JunctionData, WayData> graph
  ) {
    if (wCCs.size() > 1) {
      // completing all wcc pairs and retrieving their node representatives with the closest distance
      List<Pair<WeaklyConnectedComponent, WeaklyConnectedComponent>> ccsToConnect = findAllRequiredConnections(wCCs);
      List<Pair<Node<JunctionData, WayData>, Node<JunctionData, WayData>>> nodesToConnect = ccsToConnect.stream()
          .map(pair -> findClosestNodes(pair.getLeft(), pair.getRight(), graph))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toList();

      // creating two edges in different directions between all pairs of nodes and adding to graph
      nodesToConnect.stream()
          .map(nodesPair -> createBothEdgesBetweenNodes(nodesPair.getLeft(), nodesPair.getRight()))
          .flatMap(edgePair -> Stream.of(edgePair.getLeft(), edgePair.getRight()))
          .forEach(edge -> {
            if (!graph.getEdges().containsKey(edge.getId())) {
              graph.addEdge(edge);
            }
          });
    }

    return graph;
  }

  private List<Pair<WeaklyConnectedComponent, WeaklyConnectedComponent>> findAllRequiredConnections (
      List<WeaklyConnectedComponent> wCCs
  ) {
    return IntStream.range(0, wCCs.size() - 1)
        .mapToObj(i -> Pair.of(i, IntStream.range(i + 1, wCCs.size())
            .boxed()
            .toList()))
        .flatMap(pair -> pair.getRight().stream()
            .map(index -> Pair.of(pair.getLeft(), index)))
        .map(pair -> Pair.of(wCCs.get(pair.getLeft()), wCCs.get(pair.getRight())))
        .toList();
  }

  private Optional<Pair<Node<JunctionData, WayData>, Node<JunctionData, WayData>>> findClosestNodes(
      WeaklyConnectedComponent wCC1, WeaklyConnectedComponent wCC2, Graph<JunctionData, WayData> graph
  ) {
    return wCC1.getNodesIds().stream()
        .flatMap(firstId -> wCC2.getNodesIds().stream()
            .map(secondId -> Pair.of(firstId, secondId)))
        .map(pair -> Pair.of(graph.getNodes().get(pair.getLeft()), graph.getNodes().get(pair.getRight())))
        .min(Comparator.comparingDouble(pair -> Point.convertFromCoords(pair.getLeft().getData()).distanceTo(
            Point.convertFromCoords(pair.getRight().getData()))));
  }

  private Pair<Edge<JunctionData, WayData>, Edge<JunctionData, WayData>> createBothEdgesBetweenNodes(
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

    return Pair.of(edge1, edge2);
  }

  private Map<String, String> retrieveTagsUsingNodes(
      Node<JunctionData, WayData> node1, Node<JunctionData, WayData> node2
  ) {
    Collection<String> tagsToRetrieve = List.of("highway", "name", "addr:country", "maxspeed");
    Map<String, String> newTags = new HashMap<>();

    Collection<Entry<String, String>> joinedTagsFromEdges = Stream.of(
        node1.getIncomingEdges(), node1.getOutgoingEdges(), node2.getIncomingEdges(), node2.getOutgoingEdges())
        .flatMap(List::stream)
        .map(edge -> edge.getData().getTags())
        .flatMap(map -> map.entrySet().stream())
        .toList();

    tagsToRetrieve.forEach(tag ->
        getMostOccurredTag(joinedTagsFromEdges, tag)
            .ifPresent(result -> newTags.put(tag, result)));

    return newTags;
  }

  private Optional<String> getMostOccurredTag(Collection<Entry<String, String>> tags, String key) {
    return tags.stream()
        .filter(entry -> entry.getKey().equals(key))
        .map(Entry::getValue)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .max(Comparator.comparingLong(Entry::getValue))
        .map(Entry::getKey);
  }
}
