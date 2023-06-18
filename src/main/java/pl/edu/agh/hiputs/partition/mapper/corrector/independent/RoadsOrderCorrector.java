package pl.edu.agh.hiputs.partition.mapper.corrector.independent;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.sort.EdgeSorter;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(4)
@RequiredArgsConstructor
public class RoadsOrderCorrector implements Corrector {
  private final EdgeSorter edgeSorter;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    // sorting incoming edges if there is more than 1
    graph.getNodes().values().forEach(node -> {
      Optional.of(node)
          .filter(nodeToCheck -> nodeToCheck.getIncomingEdges().size() >= 2)
          .ifPresent(checkedNode -> {
            List<Edge<JunctionData, WayData>> sortedIncomingEdges = edgeSorter.getSorted(
                checkedNode.getIncomingEdges(),
                checkedNode.getIncomingEdges().get(0),
                incomingEdge -> incomingEdge.getSource().getData(),
                incomingEdge -> incomingEdge.getTarget().getData()
            );

            checkedNode.getIncomingEdges().clear();
            checkedNode.getIncomingEdges().addAll(sortedIncomingEdges);
          });

      // sorting outgoing edges if there is more than 1
      Optional.of(node)
          .filter(nodeToCheck -> nodeToCheck.getOutgoingEdges().size() >= 2)
          .ifPresent(checkedNode -> {
            List<Edge<JunctionData, WayData>> sortedOutgoingEdges = edgeSorter.getSorted(
                checkedNode.getOutgoingEdges(),
                checkedNode.getOutgoingEdges().get(0),
                outgoingEdge -> outgoingEdge.getTarget().getData(),
                outgoingEdge -> outgoingEdge.getSource().getData()
            );

            checkedNode.getOutgoingEdges().clear();
            checkedNode.getOutgoingEdges().addAll(sortedOutgoingEdges);
          });
    });

    return graph;
  }
}
