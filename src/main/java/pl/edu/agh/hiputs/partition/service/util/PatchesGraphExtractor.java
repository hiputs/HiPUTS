package pl.edu.agh.hiputs.partition.service.util;

import de.topobyte.osm4j.core.model.impl.Way;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
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
    GraphPreconditionsChecker.checkPreconditions(inputGraph);

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

}
