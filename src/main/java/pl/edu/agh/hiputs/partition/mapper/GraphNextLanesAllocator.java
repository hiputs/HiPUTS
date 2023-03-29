package pl.edu.agh.hiputs.partition.mapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.util.turn.TurnDirection;
import pl.edu.agh.hiputs.partition.mapper.util.turn.TurnProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(4)
@RequiredArgsConstructor
public class GraphNextLanesAllocator implements GraphTransformer {
  private final TurnProcessor turnProcessor;

  @Override
  public Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph) {
    graph.getNodes().values().stream()
        .filter(node -> node.getData().isCrossroad())
        .forEach(this::allocateSuccessorsOnCrossroad);

    graph.getNodes().values().stream()
        .filter(node -> !node.getData().isCrossroad())
        .forEach(this::allocateSuccessorsOnBend);

    return graph;
  }

  private void allocateSuccessorsOnCrossroad(Node<JunctionData, WayData> crossroad) {
    crossroad.getIncomingEdges().forEach(incomingEdge -> {
      List<List<TurnDirection>> availableTurns = turnProcessor.getTurnDirectionsFromTags(incomingEdge.getData());
      Optional.of(availableTurns)
          .filter(list -> !list.isEmpty())
          .ifPresentOrElse(list -> {
            Set<TurnDirection> distinctTurns = list.stream()
                .flatMap(List::stream)
                .sorted()
                .collect(Collectors.toCollection(TreeSet::new));
            if (distinctTurns.size() != crossroad.getOutgoingEdges().size()) {

            }
          }, () -> {

          });
    });
  }

  private void allocateSuccessorsOnBend(Node<JunctionData, WayData> bend) {
    // merge lanes also included here
  }
}
