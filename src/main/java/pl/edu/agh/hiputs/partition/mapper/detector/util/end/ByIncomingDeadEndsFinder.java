package pl.edu.agh.hiputs.partition.mapper.detector.util.end;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
public class ByIncomingDeadEndsFinder implements DeadEndsFinder{

  @Override
  public List<DeadEnd> lookup(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream()
        .filter(node -> node.getOutgoingEdges().isEmpty() && node.getIncomingEdges().size() == 1)
        .map(node -> new DeadEnd(node, findIncomingRoad(node)))
        .toList();
  }

  private List<Edge<JunctionData, WayData>> findIncomingRoad(Node<JunctionData, WayData> startingNode) {
    List<Edge<JunctionData, WayData>> builtRoad = new ArrayList<>();
    Node<JunctionData, WayData> currentNode = startingNode;

    while (currentNode.getIncomingEdges().size() == 1 && currentNode.getOutgoingEdges().size() <= 1) {
      Edge<JunctionData, WayData> incomingEdge = currentNode.getIncomingEdges().get(0);

      builtRoad.add(incomingEdge);
      currentNode = incomingEdge.getSource();
    }

    return builtRoad;
  }
}
