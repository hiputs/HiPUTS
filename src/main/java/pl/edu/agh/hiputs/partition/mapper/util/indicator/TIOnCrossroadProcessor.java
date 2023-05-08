package pl.edu.agh.hiputs.partition.mapper.util.indicator;

import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.lights.control.StandardSignalsControlCenter;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicator;

@Service
@Order(1)
public class TIOnCrossroadProcessor implements TIProcessor, TIAllocator{

  @Override
  public void checkAndAllocate(Node<JunctionData, WayData> node) {
    if (node.getData().isCrossroad()) {
      allocateAroundNode(node);
    }
  }

  @Override
  public void allocateAroundNode(Node<JunctionData, WayData> node) {
    node.getIncomingEdges().forEach(incomingEdge ->
        incomingEdge.getData().setTrafficIndicator(Optional.of(TrafficIndicator.builder().build())));

    node.getData().setSignalsControlCenter(Optional.of(StandardSignalsControlCenter.builder().build()));
  }
}
