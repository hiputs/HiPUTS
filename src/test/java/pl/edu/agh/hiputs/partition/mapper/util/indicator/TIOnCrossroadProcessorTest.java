package pl.edu.agh.hiputs.partition.mapper.util.indicator;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.edu.agh.hiputs.partition.mapper.util.indicator.helper.SimulationTimeStepGetter;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.SignalsConfigurationService;

public class TIOnCrossroadProcessorTest {
  private final SignalsConfigurationService signalConfigService = Mockito.mock(SignalsConfigurationService.class);
  private final SimulationTimeStepGetter simulationTimeStepGetter = Mockito.mock(SimulationTimeStepGetter.class);
  private final TIOnCrossroadProcessor processor = new TIOnCrossroadProcessor(
      signalConfigService, simulationTimeStepGetter);

  @Test
  public void allocateOnBend() {
    // given
    Node<JunctionData, WayData> bend = new Node<>("0", JunctionData.builder().isCrossroad(false).build());

    // when
    processor.checkAndAllocate(bend);

    // then
    Assertions.assertTrue(bend.getData().getSignalsControlCenter().isEmpty());
  }

  @Test
  public void allocateOnCrossroad() {
    // given
    Node<JunctionData, WayData> bend = new Node<>("0", JunctionData.builder().isCrossroad(true).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());

    // when
    bend.getIncomingEdges().addAll(List.of(edge1, edge2));

    Mockito.when(signalConfigService.getTimeForSpecificNode(Mockito.any())).thenReturn(0);
    Mockito.when(simulationTimeStepGetter.get()).thenReturn(1);
    processor.checkAndAllocate(bend);

    // then
    Assertions.assertTrue(bend.getData().getSignalsControlCenter().isPresent());
    Assertions.assertTrue(edge1.getData().getTrafficIndicator().isPresent());
    Assertions.assertTrue(edge2.getData().getTrafficIndicator().isPresent());
  }
}
