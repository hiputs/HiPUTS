package pl.edu.agh.hiputs.partition.mapper.util.indicator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.util.indicator.helper.SimulationTimeStepGetter;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.lights.control.StandardSignalsControlCenter;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicator;
import pl.edu.agh.hiputs.service.SignalsConfigurationService;

@Service
@Order(1)
@RequiredArgsConstructor
public class TIOnCrossroadProcessor implements TIProcessor, TIAllocator{
  private final SignalsConfigurationService signalConfigService;
  private final SimulationTimeStepGetter simulationTimeStepGetter;

  @Override
  public void checkAndAllocate(Node<JunctionData, WayData> node) {
    if (node.getData().isCrossroad()) {
      allocateAroundNode(node);
    }
  }

  @Override
  public void allocateAroundNode(Node<JunctionData, WayData> node) {
    node.getIncomingEdges().forEach(incomingEdge ->
        incomingEdge.getData().setTrafficIndicator(Optional.of(new TrafficIndicator())));

    node.getData().setSignalsControlCenter(Optional.of(new StandardSignalsControlCenter(
            signalConfigService.getTimeForSpecificNode(node.getId()) / simulationTimeStepGetter.get()
        )));
  }
}
