package pl.edu.agh.hiputs.partition.mapper.util.indicator;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(2)
@RequiredArgsConstructor
public class TIOnRoadProcessor implements TIProcessor{
  private final TIDeterminer trafficIndicatorDeterminer;
  private final TIAllocator onCrossroadAllocator;

  @Override
  public void checkAndAllocate(Node<JunctionData, WayData> node) {
    if (!node.getData().isCrossroad()) {
      allocateOnTheClosestNode(node);
    }
  }

  private void allocateOnTheClosestNode(Node<JunctionData, WayData> node) {
    node.getOutgoingEdges().stream()
        .map(this::findClosestNonSignalCrossroad)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(onCrossroadAllocator::allocateAroundNode);
  }

  private Node<JunctionData, WayData> findClosestNonSignalCrossroad(Edge<JunctionData, WayData> startEdge) {
    Edge<JunctionData, WayData> currentEdge = startEdge;

    while(!currentEdge.getTarget().getData().isCrossroad()) {
      final Node<JunctionData, WayData> source = currentEdge.getSource();

      currentEdge = currentEdge.getTarget().getOutgoingEdges().stream()
          .filter(outgoingEdge -> !outgoingEdge.getTarget().equals(source))
          .findAny()
          .orElse(null);

      if (currentEdge == null || trafficIndicatorDeterminer.checkFromTags(currentEdge.getTarget().getData().getTags())) {
        return null;
      }
    }

    return trafficIndicatorDeterminer.checkFromTags(currentEdge.getTarget().getData().getTags()) ?
        null : currentEdge.getTarget();
  }
}
