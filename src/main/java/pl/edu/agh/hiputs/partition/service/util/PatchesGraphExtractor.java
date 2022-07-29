package pl.edu.agh.hiputs.partition.service.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Slf4j
public class PatchesGraphExtractor {

  public Graph<PatchData, PatchConnectionData> createFrom(Graph<JunctionData, WayData> inputGraph) {
    checkPreconditions(inputGraph);

    Map<String, Graph.GraphBuilder<JunctionData, WayData>> builders = new HashMap<>();

    // initialize graph builders
    inputGraph.getEdges()
        .values()
        .stream()
        .map(e -> e.getData().getPatchId())
        .collect(Collectors.toSet())
        .forEach(patchId -> builders.put(patchId, new GraphBuilder<>()));

    // create patches graph nodes
    inputGraph.getNodes().values().forEach(node -> builders.get(node.getData().getPatchId()).addNode(node));

    // create patches graph edges
    inputGraph.getEdges().values().forEach(edge -> builders.get(edge.getData().getPatchId()).addEdge(edge));

    // some variables
    Graph.GraphBuilder<PatchData, PatchConnectionData> patchesGraphBuilder = new Graph.GraphBuilder<>();
    Map<String, Node<PatchData, PatchConnectionData>> patchId2patch = new HashMap<>();

    // add nodes to patches graph
    builders.forEach((patchId, graphBuilder) -> {
      Node<PatchData, PatchConnectionData> patchNode =
          new Node<>(patchId, PatchData.builder().graphInsidePatch(graphBuilder.build()).build());

      patchId2patch.put(patchId, patchNode);
      patchesGraphBuilder.addNode(patchNode);
    });

    // detect edges on patches graph
    inputGraph.getNodes()
        .values()
        .stream()
        .flatMap(node -> {
          List<Pair<Edge<JunctionData, WayData>, Edge<JunctionData, WayData>>> res = new LinkedList<>();
          for (Edge<JunctionData, WayData> inEdge : node.getIncomingEdges()) {
            for (Edge<JunctionData, WayData> outEdge : node.getOutgoingEdges()) {
              if (!inEdge.getData().getPatchId().equals(outEdge.getData().getPatchId())) {
                res.add(Pair.of(inEdge, outEdge));
              }
            }
          }
          return res.stream();
        })
        .collect(Collectors.groupingBy(
            p -> Pair.of(p.getLeft().getData().getPatchId(), p.getRight().getData().getPatchId())))
        .forEach((key, value) -> {
          PatchConnectionData patchConnectionData = PatchConnectionData.builder()
              .throughput(
                  (double) value.stream().map(e -> e.getLeft().getData().getMaxSpeed()).reduce(Integer::sum).get())
              .build();
          Edge<PatchData, PatchConnectionData> newPatchConnection =
              new Edge<>(key.getLeft() + "->" + key.getRight(), patchConnectionData);
          newPatchConnection.setSource(patchId2patch.get(key.getLeft()));
          newPatchConnection.setTarget(patchId2patch.get(key.getRight()));
          patchesGraphBuilder.addEdge(newPatchConnection);
        });

    return patchesGraphBuilder.build();
  }

  private void checkPreconditions(Graph<JunctionData, WayData> inputGraph) {
    Set<Node<JunctionData, WayData>> unassignedNodes = inputGraph.getNodes()
        .values()
        .stream()
        .filter(node -> node.getData().getPatchId() == null)
        .collect(Collectors.toSet());

    Set<Edge<JunctionData, WayData>> unassignedEdges = inputGraph.getEdges()
        .values()
        .stream()
        .filter(edge -> edge.getData().getPatchId() == null)
        .collect(Collectors.toSet());

    String unassignedNodesString = unassignedNodes.stream().map(Node::getId).collect(Collectors.joining(" "));
    String unassignedEdgesString = unassignedEdges.stream().map(Edge::getId).collect(Collectors.joining(" "));

    log.info(String.format("Node cover by colouring into patches = %f (%d/%d)",
        (double)(inputGraph.getNodes().size() - unassignedNodes.size())/inputGraph.getNodes().size(),
        inputGraph.getNodes().size() - unassignedNodes.size(),
        inputGraph.getNodes().size()));
    log.info(String.format("Edge cover by colouring into patches = %f (%d/%d)",
        (double)(inputGraph.getEdges().size() - unassignedEdges.size())/inputGraph.getEdges().size(),
        inputGraph.getEdges().size() - unassignedEdges.size(),
        inputGraph.getEdges().size()));

    if (unassignedNodes.size() > 0 && unassignedEdges.size() > 0) {
      throw new IllegalStateException(
          String.format("Unassigned nodes and edges detected. Cannot extract patches graph.\n Nodes: %s \n Edges: %s", unassignedNodesString, unassignedEdgesString));
    }

    if (unassignedNodes.size() > 0) {
      throw new IllegalStateException(String.format("Unassigned nodes detected. Cannot extract patches graph.\n Nodes: %s", unassignedNodesString));
    }

    if (unassignedEdges.size() > 0) {
      throw new IllegalStateException(
          String.format("Unassigned edges detected. Cannot extract patches graph.\n Edges: %s", unassignedEdgesString));
    }
  }

}
