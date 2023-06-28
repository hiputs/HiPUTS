package pl.edu.agh.hiputs.partition.mapper.detector.util.connectivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
public class KosarajuSCCFinder implements CCFinder<StronglyConnectedComponent> {

  @Override
  public List<StronglyConnectedComponent> lookup(Graph<JunctionData, WayData> graph) {
    Stack<String> nodeIdsStack = new Stack<>();
    Set<String> visitedNodesIds = new HashSet<>();

    // filling stack with nodes to receive an order to run next dfs
    graph.getNodes().values().forEach(node -> {
      if (!visitedNodesIds.contains(node.getId())) {
        dfsFillStack(node, nodeIdsStack, visitedNodesIds);
      }
    });

    // transposing graph
    Graph<JunctionData, WayData> transposedGraph = transposeGraph(graph);

    // detecting sCCs
    List<StronglyConnectedComponent> foundSCCs = new ArrayList<>();
    visitedNodesIds.clear();

    while (!nodeIdsStack.empty()) {
      String processingNodeId = nodeIdsStack.pop();
      Node<JunctionData, WayData> processingNode = transposedGraph.getNodes().get(processingNodeId);
      StronglyConnectedComponent scc = new StronglyConnectedComponent();

      if (!visitedNodesIds.contains(processingNodeId)) {
        dfsBuildSCC(processingNode, scc, visitedNodesIds);
      }

      if (!scc.getNodesIds().isEmpty()) {
        foundSCCs.add(scc);
      }
    }

    return foundSCCs;
  }

  private void dfsFillStack(Node<JunctionData, WayData> startNode, Stack<String> nodeStack, Set<String> visitedNodes) {
    visitedNodes.add(startNode.getId());

    startNode.getOutgoingEdges().forEach(edge -> {
      if (!visitedNodes.contains(edge.getTarget().getId())) {
        dfsFillStack(edge.getTarget(), nodeStack, visitedNodes);
      }
    });

    nodeStack.push(startNode.getId());
  }

  private void dfsBuildSCC(Node<JunctionData, WayData> startNode, StronglyConnectedComponent scc, Set<String> visitedNodes) {
    visitedNodes.add(startNode.getId());
    scc.addNode(startNode.getId());

    startNode.getOutgoingEdges().forEach(edge -> {
      if (!visitedNodes.contains(edge.getTarget().getId())) {
        dfsBuildSCC(edge.getTarget(), scc, visitedNodes);
      } else {
        scc.addExternalEdge(edge.getId());
      }
    });
  }

  private Graph<JunctionData, WayData> transposeGraph(Graph<JunctionData, WayData> graph) {
    GraphBuilder<JunctionData, WayData> newGraphBuilder = new GraphBuilder<>();

    graph.getNodes().values().forEach(node ->
        newGraphBuilder.addNode(new Node<>(node.getId(), node.getData())));

    graph.getEdges().values().forEach(edge -> {
      Edge<JunctionData, WayData> newEdge = new Edge<>(edge.getId(), edge.getData());
      newEdge.setSource(edge.getTarget());
      newEdge.setTarget(edge.getSource());
      newGraphBuilder.addEdge(newEdge);
    });

    return newGraphBuilder.build();
  }
}
