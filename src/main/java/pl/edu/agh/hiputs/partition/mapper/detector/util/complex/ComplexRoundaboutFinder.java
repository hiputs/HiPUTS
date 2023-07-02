package pl.edu.agh.hiputs.partition.mapper.detector.util.complex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroad;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
public class ComplexRoundaboutFinder implements ComplexityFinder{
  private final static String junctionKey = "junction";
  private final static Set<String> roundaboutValues = Set.of("roundabout", "circular");

  @Override
  public List<ComplexCrossroad> lookup(Graph<JunctionData, WayData> graph) {
    // retrieving all edges and nodes contained in roundabouts
    Set<Edge<JunctionData, WayData>> roundaboutEdges = graph.getEdges().values().stream()
        .filter(this::isInRoundabout)
        .collect(Collectors.toSet());
    Set<Node<JunctionData, WayData>> roundaboutNodes = roundaboutEdges.stream()
        .flatMap(edge -> Stream.of(edge.getSource(), edge.getTarget()))
        .collect(Collectors.toSet());

    // preparing for dfs
    List<ComplexCrossroad> complexCrossroads = new ArrayList<>();
    Set<String> visitedNodes = new HashSet<>();

    // running dfs on all non-visited nodes being in roundabouts, every call is separate complex roundabout
    roundaboutNodes.forEach(node -> {
      if (!visitedNodes.contains(node.getId())) {
        ComplexCrossroad foundRoundabout = new ComplexCrossroad();

        dfs(node, foundRoundabout, visitedNodes, roundaboutEdges);

        complexCrossroads.add(foundRoundabout);
      }
    });

    // returning roundabouts with more than one node containing
    return complexCrossroads.stream()
        .filter(foundCrossroad -> foundCrossroad.getNodesIdsIn().size() > 1)
        .toList();
  }

  private void dfs(
      Node<JunctionData, WayData> startingNode, ComplexCrossroad complexCrossroad,
      Set<String> visitedNodes, Set<Edge<JunctionData, WayData>> roundAboutEdges
  ) {
    visitedNodes.add(startingNode.getId());
    complexCrossroad.addNode(startingNode.getId());

    getNeighbours(startingNode, roundAboutEdges).forEach(neighbour -> {
      if (!visitedNodes.contains(neighbour.getId())) {
        dfs(neighbour, complexCrossroad, visitedNodes, roundAboutEdges);
      }
    });
  }

  private Collection<Node<JunctionData, WayData>> getNeighbours(
      Node<JunctionData, WayData> node, Set<Edge<JunctionData, WayData>> roundAboutEdges
  ) {
    return Stream.concat(
        node.getOutgoingEdges().stream()
            .filter(roundAboutEdges::contains)
            .map(Edge::getTarget),
        node.getIncomingEdges().stream()
            .filter(roundAboutEdges::contains)
            .map(Edge::getSource)
    ).toList();
  }

  private boolean isInRoundabout(Edge<JunctionData, WayData> edge) {
    return edge.getData().getTags().containsKey(junctionKey) &&
        roundaboutValues.contains(edge.getData().getTags().get(junctionKey));
  }
}
