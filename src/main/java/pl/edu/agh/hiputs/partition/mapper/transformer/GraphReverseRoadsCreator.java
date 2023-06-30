package pl.edu.agh.hiputs.partition.mapper.transformer;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.reflector.EdgeReflector;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(4)
@RequiredArgsConstructor
public class GraphReverseRoadsCreator implements GraphTransformer{
  private final EdgeReflector edgeReflector;

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
        .map(edgeReflector::reverseEdge)
        .forEach(newGraphBuilder::addEdge);

    // adding reverse road to dead ends
    graph.getNodes().values().stream()
        .filter(node -> node.getOutgoingEdges().size() == 0)
        .flatMap(node -> node.getIncomingEdges().stream())
        .map(edgeReflector::reverseEdge)
        .forEach(newGraphBuilder::addEdge);

    return newGraphBuilder.build();
  }
}
