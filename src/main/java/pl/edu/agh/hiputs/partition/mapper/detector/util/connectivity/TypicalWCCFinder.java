package pl.edu.agh.hiputs.partition.mapper.detector.util.connectivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
public class TypicalWCCFinder implements CCFinder<WeaklyConnectedComponent>{

  @Override
  public List<WeaklyConnectedComponent> lookup(Graph<JunctionData, WayData> graph) {
    List<WeaklyConnectedComponent> wCCs = new ArrayList<>();
    Set<String> visitedNodesIds = new HashSet<>();

    graph.getNodes().values().forEach(node -> {
      if (!visitedNodesIds.contains(node.getId())) {
        WeaklyConnectedComponent wCC = new WeaklyConnectedComponent();

        dfs(node, wCC, visitedNodesIds);

        wCCs.add(wCC);
      }
    });

    return wCCs;
  }

  private void dfs(Node<JunctionData, WayData> startNode, WeaklyConnectedComponent wCC, Set<String> visitedNodes) {
    visitedNodes.add(startNode.getId());
    wCC.addNode(startNode.getId());

    getNeighbours(startNode).forEach(neighbour -> {
      if (!visitedNodes.contains(neighbour.getId())) {
        dfs(neighbour, wCC, visitedNodes);
      }
    });
  }

  private Collection<Node<JunctionData, WayData>> getNeighbours(Node<JunctionData, WayData> node) {
    return Stream.concat(
        node.getOutgoingEdges().stream()
            .map(Edge::getTarget),
        node.getIncomingEdges().stream()
            .map(Edge::getSource)
    ).toList();
  }
}
