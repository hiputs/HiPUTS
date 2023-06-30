package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.reflector.EdgeReflector;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@RequiredArgsConstructor
public class DirectedBridgesCreator implements BridgesCreator{
  private final EdgeReflector edgeReflector;

  @Override
  public Graph<JunctionData, WayData> createBetweenCCsOnGraph(
      List<StronglyConnectedComponent> sCCs,
      List<WeaklyConnectedComponent> wCCs,
      Graph<JunctionData, WayData> graph
  ) {
    if (sCCs.size() > 1) {
      createDirectedBridgesOnGraph(sCCs, graph).forEach(edge -> {
        if (!graph.getEdges().containsKey(edge.getId())) {
          graph.addEdge(edge);
        }
      });
    }

    return graph;
  }

  private List<Edge<JunctionData, WayData>> createDirectedBridgesOnGraph(
      List<StronglyConnectedComponent> sCCs, Graph<JunctionData, WayData> graph
  ) {
    // creating map of node containing in scc to this scc
    Map<String, StronglyConnectedComponent> nodesIds2SCC = sCCs.stream()
        .flatMap(scc -> scc.getNodesIds().stream()
            .map(nodeId -> Pair.of(nodeId, scc)))
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

    // using above map, creating map of all external edges to the pointing scc
    Map<String, StronglyConnectedComponent> externalEdgesIds2PointingSCC = sCCs.stream()
        .flatMap(scc -> scc.getExternalEdgesIds().stream()
            .map(edgeId -> Pair.of(edgeId, nodesIds2SCC.get(graph.getEdges().get(edgeId).getSource().getId()))))
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

    // creating bridges by reversing external edge, only one for single scc
    // when multiple external edges are pointing one scc - choosing this one with minimum length
    return sCCs.stream()
        .filter(scc -> !scc.getExternalEdgesIds().isEmpty())
        .flatMap(scc -> uniqueMinExternalEdges(scc.getExternalEdgesIds(), externalEdgesIds2PointingSCC, graph))
        .map(externalEdgeId -> graph.getEdges().get(externalEdgeId))
        .map(edgeReflector::reverseEdge)
        .collect(Collectors.toList());
  }

  private Stream<String> uniqueMinExternalEdges(
      Set<String> edgesIds,
      Map<String, StronglyConnectedComponent> externalEdgesIds2PointingSCC,
      Graph<JunctionData, WayData> graph
  ) {
    // grouping edges in list by pointing scc, then reducing list to single id of edge with minimum length
    return edgesIds.stream()
        .map(edgeId -> Pair.of(edgeId, externalEdgesIds2PointingSCC.get(edgeId)))
        .collect(Collectors.groupingBy(Pair::getRight, Collectors.mapping(Pair::getLeft, Collectors.toList())))
        .entrySet().stream()
        .map(entry -> Map.entry(entry.getKey(), idOfEdgeWithMinLength(entry.getValue(), graph)))
        .filter(entry -> entry.getValue().isPresent())
        .map(entry -> entry.getValue().get());
  }

  private Optional<String> idOfEdgeWithMinLength(List<String> edgesIds, Graph<JunctionData, WayData> graph) {
    // choosing edge with minimum length
    return edgesIds.stream()
        .map(edgeId -> graph.getEdges().get(edgeId))
        .min(Comparator.comparingDouble(edge -> edge.getData().getLength()))
        .map(Edge::getId);
  }
}
