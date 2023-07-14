package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroadsRepository;
import pl.edu.agh.hiputs.partition.mapper.helper.service.crossroad.StandardCrossroadDeterminer;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroad;
import pl.edu.agh.hiputs.partition.mapper.transformer.GraphCrossroadDeterminer;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.geom.Point;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@RequiredArgsConstructor
public class ComplexCrossroadsCorrector implements Corrector {
  private final GraphCrossroadDeterminer graphCrossroadDeterminer =
      new GraphCrossroadDeterminer(new StandardCrossroadDeterminer());
  private final ComplexCrossroadsRepository complexCrossroadsRepository;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    // starting transformation - external edges extraction and new node instead of old nodes group creation
    List<CrossroadTransformation> preparedTransformations = complexCrossroadsRepository.getComplexCrossroads().stream()
        .filter(crossroad -> crossroad.getNodesIdsIn().stream().allMatch(nodeId -> graph.getNodes().containsKey(nodeId)))
        .map(crossroad -> transform(crossroad, graph))
        .toList();

    // collecting all nodes to replace and edges to change their source/target
    Map<Node<JunctionData, WayData>, Node<JunctionData, WayData>> oldNode2NewNode = preparedTransformations.stream()
        .flatMap(crossroadTransformation -> crossroadTransformation.getOldNodes().stream()
            .map(node -> Pair.of(node, crossroadTransformation.getNewNode())))
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    Set<Edge<JunctionData, WayData>> excludedEdges = preparedTransformations.stream()
        .flatMap(crossroadTransformation -> crossroadTransformation.getOldEdges().stream())
        .collect(Collectors.toSet());

    // starting graph creation
    GraphBuilder<JunctionData, WayData> graphBuilder = new GraphBuilder<>();

    // adding all non-changed nodes
    graph.getNodes().values().forEach(node -> {
      if (!oldNode2NewNode.containsKey(node)) {
        graphBuilder.addNode(recreateNode(node));   // recreation for cleanup incoming/outgoing edges
      }
    });

    // adding new nodes representing simpler crossroads
    preparedTransformations.forEach(transformation ->
        graphBuilder.addNode(transformation.getNewNode()));

    // adding all non-changed edges
    graph.getEdges().values().forEach(edge -> {
      if (!excludedEdges.contains(edge)) {
        graphBuilder.addEdge(edge);
      }
    });

    // adding external edges pointing to new nodes representing simpler crossroads
    preparedTransformations.forEach(transformation -> {
      transformation.getExternalIncomingEdges().stream()
          .map(edge -> recreateEdge(edge,     // recreation for new id and length
              oldNode2NewNode.getOrDefault(edge.getSource(), edge.getSource()),
              oldNode2NewNode.getOrDefault(transformation.getNewNode(), transformation.getNewNode())))
          .forEach(graphBuilder::addEdge);
      transformation.getExternalOutgoingEdges().stream()
          .map(edge -> recreateEdge(edge,     // recreation for new id and length
              oldNode2NewNode.getOrDefault(transformation.getNewNode(), transformation.getNewNode()),
              oldNode2NewNode.getOrDefault(edge.getTarget(), edge.getTarget())))
          .forEach(graphBuilder::addEdge);
    });

    return graphCrossroadDeterminer.transform(graphBuilder.build());
  }

  private CrossroadTransformation transform(ComplexCrossroad complexCrossroad, Graph<JunctionData, WayData> graph) {
    CrossroadTransformation transformation = new CrossroadTransformation();

    // needed for filtering out during new graph building
    transformation.setOldNodes(complexCrossroad.getNodesIdsIn().stream()
        .map(nodeId -> graph.getNodes().get(nodeId))
        .collect(Collectors.toSet()));

    // needed for filtering out during new graph building
    transformation.setOldEdges(transformation.getOldNodes().stream()
        .flatMap(node -> Stream.concat(node.getIncomingEdges().stream(), node.getOutgoingEdges().stream()))
        .collect(Collectors.toSet()));

    // new node with merged data representing new simpler crossroad
    transformation.setNewNode(new Node<>(UUID.randomUUID().toString(),
        mergeAllJunctionData(transformation.getOldNodes().stream()
            .map(Node::getData)
            .toList())
    ));

    // external incoming edges, they must point new node at target
    transformation.setExternalIncomingEdges(transformation.getOldNodes().stream()
        .flatMap(node -> node.getIncomingEdges().stream())
        .filter(edge -> !transformation.getOldNodes().contains(edge.getSource()))
        .collect(Collectors.toSet()));

    // external outgoing edges, they must point new node at source
    transformation.setExternalOutgoingEdges(transformation.getOldNodes().stream()
        .flatMap(node -> node.getOutgoingEdges().stream())
        .filter(edge -> !transformation.getOldNodes().contains(edge.getTarget()))
        .collect(Collectors.toSet()));

    return transformation;
  }

  private Edge<JunctionData, WayData> recreateEdge(
      Edge<JunctionData, WayData> edge, Node<JunctionData, WayData> source, Node<JunctionData, WayData> target
  ) {
    WayData newWayData = WayData.builder()
        .isOneWay(edge.getData().isOneWay())
        .tagsInOppositeMeaning(edge.getData().isTagsInOppositeMeaning())
        .tags(edge.getData().getTags())
        .length(Point.convertFromCoords(source.getData()).distanceTo(Point.convertFromCoords(target.getData())))
        .maxSpeed(edge.getData().getMaxSpeed())
        .isPriorityRoad(edge.getData().isPriorityRoad())
        .patchId(edge.getData().getPatchId())
        .build();

    Edge<JunctionData, WayData> newEdge = new Edge<>(source.getId() + "->" + target.getId(), newWayData);
    newEdge.setSource(source);
    newEdge.setTarget(target);

    return newEdge;
  }

  private Node<JunctionData, WayData> recreateNode(Node<JunctionData, WayData> node) {
    return new Node<>(node.getId(), node.getData());
  }

  private JunctionData mergeAllJunctionData(List<JunctionData> allJunctionData) {
    Map<String, Map<String, Long>> keyValueStatistics = allJunctionData.stream()
        .flatMap(junctionData -> junctionData.getTags().entrySet().stream())
        .collect(Collectors.groupingBy(Entry::getKey,
            Collectors.groupingBy(Entry::getValue, Collectors.counting())));

    Map<String, String> getMostUsedValuesForEachKey = keyValueStatistics.entrySet().stream()
        .map(entry -> Map.entry(entry.getKey(), entry.getValue().entrySet().stream()
            .max(Comparator.comparingLong(Entry::getValue))
            .map(Entry::getKey)))
        .filter(entry -> entry.getValue().isPresent())
        .map(entry -> Map.entry(entry.getKey(), entry.getValue().get()))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    double latitude = allJunctionData.stream()
        .map(JunctionData::getLat)
        .mapToDouble(lat -> lat)
        .average()
        .orElseThrow();

    double longitude = allJunctionData.stream()
        .map(JunctionData::getLon)
        .mapToDouble(lon -> lon)
        .average()
        .orElseThrow();

    return JunctionData.builder()
        .isCrossroad(true)
        .tags(getMostUsedValuesForEachKey)
        .lat(latitude)
        .lon(longitude)
        .build();
  }

  @Getter
  @Setter
  private static class CrossroadTransformation {
    private Set<Node<JunctionData, WayData>> oldNodes;
    private Set<Edge<JunctionData, WayData>> oldEdges;


    private Set<Edge<JunctionData, WayData>> externalIncomingEdges;
    private Set<Edge<JunctionData, WayData>> externalOutgoingEdges;
    private Node<JunctionData, WayData> newNode;
  }
}
