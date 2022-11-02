package pl.edu.agh.hiputs.partition.service.util;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class GraphPreconditionsChecker {

  public static void checkPreconditions(Graph<JunctionData, WayData> inputGraph) {
    allNodesInGraphHaveAssignedPatchId(inputGraph);
    allEdgesInGraphHaveAssignedPatchId(inputGraph);
    setOfEdgesConnectedToNodesIsEqualToEdgesSetFromGraph(inputGraph);
    setOfNodesConnectedToEdgesIsEqualToNodesSetFromGraph(inputGraph);
    inGraphThereIsNoSingleNodePatches(inputGraph);
  }

  private static void allNodesInGraphHaveAssignedPatchId(Graph<JunctionData, WayData> inputGraph) {
    Set<Node<JunctionData, WayData>> unassignedNodes = inputGraph.getNodes()
        .values()
        .stream()
        .filter(node -> node.getData().getPatchId() == null)
        .collect(Collectors.toSet());

    String unassignedNodesString = unassignedNodes.stream().map(Node::getId).collect(Collectors.joining(" "));

    if (unassignedNodes.size() > 0) {
      throw new IllegalStateException(String.format("Unassigned nodes detected. Cannot extract patches graph.\n Nodes: %s", unassignedNodesString));
    }
  }

  private static void allEdgesInGraphHaveAssignedPatchId(Graph<JunctionData, WayData> inputGraph) {
    Set<Edge<JunctionData, WayData>> unassignedEdges = inputGraph.getEdges()
        .values()
        .stream()
        .filter(edge -> edge.getData().getPatchId() == null)
        .collect(Collectors.toSet());

    String unassignedEdgesString = unassignedEdges.stream().map(Edge::getId).collect(Collectors.joining(" "));

    if (unassignedEdges.size() > 0) {
      throw new IllegalStateException(
          String.format("Unassigned edges detected. Cannot extract patches graph.\n Edges: %s", unassignedEdgesString));
    }
  }

  private static void setOfEdgesConnectedToNodesIsEqualToEdgesSetFromGraph(Graph<JunctionData, WayData> inputGraph) {
    Set<Edge<JunctionData, WayData>> edgesConnectedToNodes = inputGraph.getNodes().values()
        .stream()
        .flatMap(node -> Stream.concat(node.getIncomingEdges().stream(), node.getOutgoingEdges().stream()))
        .collect(Collectors.toSet());

    Set<Edge<JunctionData, WayData>> edgesFromGraph = new HashSet<>(inputGraph.getEdges().values());

    if (!edgesConnectedToNodes.equals(edgesFromGraph)) {
      throw new IllegalStateException(
          String.format("Edges set in graph is not equal to edges connected to nodes of this graph. Edges in graph = %d, edges connected to nodes of this graph = %d ", edgesFromGraph.size(), edgesConnectedToNodes.size()));
    }
  }


  private static void setOfNodesConnectedToEdgesIsEqualToNodesSetFromGraph(Graph<JunctionData, WayData> inputGraph) {
    Set<Node<JunctionData, WayData>> nodesConnectedToEdges = inputGraph.getEdges().values()
        .stream()
        .flatMap(edge -> Stream.of(edge.getSource(), edge.getTarget()))
        .collect(Collectors.toSet());

    Set<Node<JunctionData, WayData>> nodesFromGraph = new HashSet<>(inputGraph.getNodes().values());

    if(!nodesConnectedToEdges.equals(nodesFromGraph)) {
      throw new IllegalStateException(
          String.format("Nodes set in graph is not equal to nodes connected to edges of this graph. Nodes in graph = %d, nodes connected to nodes of this graph = %d ", nodesFromGraph.size(), nodesConnectedToEdges.size()));
    }
  }

  private static void inGraphThereIsNoSingleNodePatches(Graph<JunctionData, WayData> inputGraph) {
    Set<String> patchesFromNodes = inputGraph.getNodes().values()
        .stream()
        .map(node -> node.getData().getPatchId())
        .collect(Collectors.toSet());

    Set<String> patchesFromEdges = inputGraph.getEdges().values()
        .stream()
        .map(edge -> edge.getData().getPatchId())
        .collect(Collectors.toSet());

    if(!patchesFromNodes.equals(patchesFromEdges)) {
      throw new IllegalStateException(String.format(
          "Set of nodes patches ids is no equal to set of edgesPatchesIds. Patches ids from nodes = %d, patches ids "
              + "from edges = %d", patchesFromNodes.size(), patchesFromEdges.size()));
    }
  }
}