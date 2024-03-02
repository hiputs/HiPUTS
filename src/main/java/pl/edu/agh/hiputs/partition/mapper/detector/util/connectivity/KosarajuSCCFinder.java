package pl.edu.agh.hiputs.partition.mapper.detector.util.connectivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
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
        Stack<Node<JunctionData, WayData>> dfsNodes = new Stack<>();

        // iterative version of dfs with post order tracking
        dfsNodes.push(node);
        while (!dfsNodes.empty()) {
          Node<JunctionData, WayData> processingNode = dfsNodes.peek();
          AtomicBoolean doneProcessing = new AtomicBoolean(true);
          visitedNodesIds.add(processingNode.getId());

          processingNode.getOutgoingEdges().forEach(edge -> {
            if (!visitedNodesIds.contains(edge.getTarget().getId())) {
              dfsNodes.push(edge.getTarget());
              doneProcessing.set(false);
            }
          });

          if (doneProcessing.get()) {
            dfsNodes.pop();
            nodeIdsStack.push(processingNode.getId());
          }
        }
      }
    });

    // transposing graph
    Graph<JunctionData, WayData> transposedGraph = transposeGraph(graph);

    // detecting sCCs
    List<StronglyConnectedComponent> foundSCCs = new ArrayList<>();
    visitedNodesIds.clear();

    while (!nodeIdsStack.empty()) {
      String startingNodeId = nodeIdsStack.pop();
      Node<JunctionData, WayData> startingNode = transposedGraph.getNodes().get(startingNodeId);
      StronglyConnectedComponent scc = new StronglyConnectedComponent();

      // port order iterative version of dfs to retrieve all nodes representing single scc
      if (!visitedNodesIds.contains(startingNodeId)) {
        Stack<Node<JunctionData, WayData>> reverseDfsNodes = new Stack<>();
        reverseDfsNodes.push(startingNode);

        while (!reverseDfsNodes.empty()) {
          Node<JunctionData, WayData> processingNode = reverseDfsNodes.pop();
          visitedNodesIds.add(processingNode.getId());
          scc.addNode(processingNode.getId());

          processingNode.getOutgoingEdges().forEach(edge -> {
            if (!visitedNodesIds.contains(edge.getTarget().getId())) {
              reverseDfsNodes.push(edge.getTarget());
            } else {
              scc.addExternalEdge(edge.getId());
            }
          });
        }
      }

      if (!scc.getNodesIds().isEmpty()) {
        foundSCCs.add(scc);
      }
    }

    return foundSCCs;
  }

  private Graph<JunctionData, WayData> transposeGraph(Graph<JunctionData, WayData> graph) {
    GraphBuilder<JunctionData, WayData> newGraphBuilder = new GraphBuilder<>();

    graph.getNodes().values().forEach(node -> newGraphBuilder.addNode(new Node<>(node.getId(), node.getData())));

    graph.getEdges().values().forEach(edge -> {
      Edge<JunctionData, WayData> newEdge = new Edge<>(edge.getId(), edge.getData());
      newEdge.setSource(edge.getTarget());
      newEdge.setTarget(edge.getSource());
      newGraphBuilder.addEdge(newEdge);
    });

    return newGraphBuilder.build();
  }
}
