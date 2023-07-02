package pl.edu.agh.hiputs.partition.mapper.detector.util.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroad;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@RequiredArgsConstructor
public class ComplexCrossroadsFinder implements ComplexityFinder{
  private final static String junctionKey = "junction";
  private final static Set<String> roundaboutValues = Set.of("roundabout", "circular");
  private final ModelConfigurationService modelConfigService;

  @Override
  public List<ComplexCrossroad> lookup(Graph<JunctionData, WayData> graph) {
    // creating graph as a workspace
    Graph<JunctionData, WayData> workingGraph = buildWorkingGraph(graph);

    // removing edges with length greater than defined in properties
    List<Edge<JunctionData, WayData>> edgesToRemove = workingGraph.getEdges().values().stream()
        .filter(edge -> edge.getData().getLength() > modelConfigService.getModelConfig().getCrossroadMinDistance())
        .toList();
    edgesToRemove.forEach(edge -> workingGraph.removeEdgeById(edge.getId()));

    // preparing for dfs
    List<ComplexCrossroad> complexCrossroads = new ArrayList<>();
    Set<String> visitedNodes = new HashSet<>();

    // running dfs on all non-visited nodes being crossroad, every call is separate complex crossroad
    workingGraph.getNodes().values().stream()
        .filter(node -> node.getData().isCrossroad())
        .forEach(node -> {
          if (!visitedNodes.contains(node.getId())) {
            ComplexCrossroad foundCrossroad = new ComplexCrossroad();

            dfs(node, foundCrossroad, visitedNodes);

            // foundCrossroad.getNodesIdsIn().removeIf(nodeId -> !graph.getNodes().get(nodeId).getData().isCrossroad());
            complexCrossroads.add(foundCrossroad);
          }
    });

    return complexCrossroads.stream()
        .filter(complexCrossroad -> complexCrossroad.getNodesIdsIn().size() > 1)
        .toList();
  }

  private void dfs(Node<JunctionData, WayData> startingNode, ComplexCrossroad complexCrossroad, Set<String> visitedNodes) {
    visitedNodes.add(startingNode.getId());
    complexCrossroad.addNode(startingNode.getId());

    getNeighbours(startingNode).forEach(neighbour -> {
      if (!visitedNodes.contains(neighbour.getId())) {
        dfs(neighbour, complexCrossroad, visitedNodes);
      }
    });
  }

  private Collection<Node<JunctionData, WayData>> getNeighbours(Node<JunctionData, WayData> node) {
    return Stream.concat(
        node.getOutgoingEdges().stream()
            .filter(this::notInRoundabout)
            .map(Edge::getTarget),
        node.getIncomingEdges().stream()
            .filter(this::notInRoundabout)
            .map(Edge::getSource))
        .filter(neighbour -> neighbour.getData().isCrossroad())
        .toList();
  }

  private boolean notInRoundabout(Edge<JunctionData, WayData> edge) {
    return !edge.getData().getTags().containsKey(junctionKey) || !roundaboutValues.contains(
        edge.getData().getTags().get(junctionKey));
  }

  private Graph<JunctionData, WayData> buildWorkingGraph(Graph<JunctionData, WayData> graph) {
    GraphBuilder<JunctionData, WayData> newGraphBuilder = new GraphBuilder<>();

    graph.getNodes().values().forEach(node -> newGraphBuilder.addNode(new Node<>(node.getId(),
        JunctionData.builder()
            .lat(node.getData().getLat())
            .lon(node.getData().getLon())
            .isCrossroad(node.getData().isCrossroad())
            .build())));

    graph.getEdges().values().forEach(edge -> {
      Edge<JunctionData, WayData> newEdge = new Edge<>(edge.getId(),
          WayData.builder().tags(edge.getData().getTags()).length(edge.getData().getLength()).build());
      newEdge.setSource(edge.getSource());
      newEdge.setTarget(edge.getTarget());
      newGraphBuilder.addEdge(newEdge);
    });

    return newGraphBuilder.build();
  }
}
