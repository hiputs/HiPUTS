package pl.edu.agh.hiputs.partition.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Slf4j
@Service
public class TrivialPatchPartitioner implements PatchPartitioner {

  private final Map<String, Node<PatchData, PatchConnectionData>> patchId2patch = new HashMap<>();

  @Override
  public Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph) {
    log.info("Partitioning into patches started");
    Graph.GraphBuilder<PatchData, PatchConnectionData> graphBuilder = new Graph.GraphBuilder<>();
    for (Edge<JunctionData, WayData> edge : graph.getEdges().values()) {
      Graph.GraphBuilder<JunctionData, WayData> graphInsidePatchBuilder = new GraphBuilder<>();

      String patchId = randomPatchId();
      if (edge.getTarget().getData().getPatchId() == null) {
        edge.getTarget().getData().setPatchId(patchId);
        graphInsidePatchBuilder.addNode(edge.getTarget());
      }

      if(edge.getSource().getIncomingEdges().isEmpty()) {
        edge.getSource().getData().setPatchId(patchId);
        graphInsidePatchBuilder.addNode(edge.getSource());
      }
      edge.getData().setPatchId(patchId);
      graphInsidePatchBuilder.addEdge(edge);

      PatchData patchData = PatchData.builder()
          .graphInsidePatch(graphInsidePatchBuilder.build())
          .build();
      Node<PatchData, PatchConnectionData> patch = new Node<>(patchId, patchData);

      patchId2patch.put(patchId, patch);
      graphBuilder.addNode(patch);
    }

    // detect edges on patch graph
    graph.getNodes().values().stream()
        .flatMap(node -> {
          List<Pair<Edge<JunctionData,WayData>,Edge<JunctionData,WayData>>> res = new LinkedList<>();
          for(Edge<JunctionData,WayData> inEdge : node.getIncomingEdges()) {
            for(Edge<JunctionData,WayData> outEdge : node.getOutgoingEdges()) {
              if (!inEdge.getData().getPatchId().equals(outEdge.getData().getPatchId())) {
                res.add(Pair.of(inEdge, outEdge));
              }
            }
          }
          return res.stream();
        }).collect(Collectors.groupingBy(p -> Pair.of(p.getLeft().getData().getPatchId(), p.getRight().getData().getPatchId())))
        .forEach((key, value) -> {
          PatchConnectionData patchConnectionData = PatchConnectionData.builder()
                              .throughput((double) value.stream().map(e -> e.getLeft().getData().getMaxSpeed()).reduce(Integer::sum).get())
                              .build();
                Edge<PatchData, PatchConnectionData> newPatchConnection =
                    new Edge<>(key.getLeft() + "->" + key.getRight(), patchConnectionData);
                newPatchConnection.setSource(patchId2patch.get(key.getLeft()));
                newPatchConnection.setTarget(patchId2patch.get(key.getRight()));
                graphBuilder.addEdge(newPatchConnection);
        });


    Graph<PatchData, PatchConnectionData> patchesGraph = graphBuilder.build();
    log.info("Partitioning into patches finished");
    return patchesGraph;
  }

  private String randomPatchId() {
    return UUID.randomUUID().toString();
  }
}
