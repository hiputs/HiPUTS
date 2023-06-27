package pl.edu.agh.hiputs.partition.mapper.transformer;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(4)
public class GraphReverseRoadsCreator implements GraphTransformer{

  // @TODO will be migrated to detector & corrector system in the future

  @Override
  public Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph) {
    Graph.GraphBuilder<JunctionData, WayData> newGraphBuilder = new Graph.GraphBuilder<>();
    graph.getNodes().values().forEach(newGraphBuilder::addNode);
    graph.getEdges().values().forEach(newGraphBuilder::addEdge);

    // adding reverse road to non-reachable start nodes
    graph.getNodes().values().stream()
        .filter(node -> node.getIncomingEdges().size() == 0)
        .flatMap(node -> node.getOutgoingEdges().stream())
        .map(this::createReversedRoad)
        .forEach(newGraphBuilder::addEdge);

    // adding reverse road to dead ends
    graph.getNodes().values().stream()
        .filter(node -> node.getOutgoingEdges().size() == 0)
        .flatMap(node -> node.getIncomingEdges().stream())
        .map(this::createReversedRoad)
        .forEach(newGraphBuilder::addEdge);

    return newGraphBuilder.build();
  }

  private Edge<JunctionData, WayData> createReversedRoad(Edge<JunctionData, WayData> edge) {
    WayData reversedData = WayData.builder()
        .isOneWay(true)
        .tagsInOppositeMeaning(!edge.getData().isTagsInOppositeMeaning())
        .tags(edge.getData().getTags())
        .length(edge.getData().getLength())
        .maxSpeed(edge.getData().getMaxSpeed())
        .isPriorityRoad(edge.getData().isPriorityRoad())
        .patchId(edge.getData().getPatchId())
        .build();

    Edge<JunctionData, WayData> reversedEdge = new Edge<>(
        edge.getTarget().getId() + "->" + edge.getSource().getId(), reversedData);
    reversedEdge.setSource(edge.getTarget());
    reversedEdge.setTarget(edge.getSource());

    return reversedEdge;
  }
}
