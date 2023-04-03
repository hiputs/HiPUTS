package pl.edu.agh.hiputs.partition.mapper;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(5)
public class GraphCrossroadDeterminer implements GraphTransformer{

  @Override
  public Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph) {
    graph.getNodes().values().forEach(node -> node.getData().setCrossroad(determineIsCrossroad(node)));

    return graph;
  }

  private boolean determineIsCrossroad(Node<JunctionData, WayData> node) {
    if (node.getIncomingEdges().size() == 0 || node.getOutgoingEdges().size() == 0) {
      return false;
    }
    else if (node.getOutgoingEdges().size() == 2 && node.getIncomingEdges().size() == 1) {
      return node.getOutgoingEdges().stream()
          .noneMatch(edge -> node.getIncomingEdges().get(0).getSource().equals(edge.getTarget()));
    }
    else if (node.getIncomingEdges().size() == 2 && node.getOutgoingEdges().size() == 1) {
      return node.getIncomingEdges().stream()
          .noneMatch(edge -> node.getOutgoingEdges().get(0).getTarget().equals(edge.getSource()));
    }
    else if (node.getOutgoingEdges().size() == 2 && node.getIncomingEdges().size() == 2) {
      return !node.getOutgoingEdges().stream()
          .allMatch(outgoingEdge -> node.getIncomingEdges().stream()
              .anyMatch(incomingEdge -> outgoingEdge.getTarget().equals(incomingEdge.getSource())));
    }
    return node.getIncomingEdges().size() > 2 || node.getOutgoingEdges().size() > 2;
  }
}
