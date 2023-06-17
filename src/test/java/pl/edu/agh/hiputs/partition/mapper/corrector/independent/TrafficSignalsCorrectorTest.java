package pl.edu.agh.hiputs.partition.mapper.corrector.independent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.component.MixedTwoAngleIndexGGRExtractor;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.StandardGreenGroupsAggregator;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.component.TwoByAngleGGRExtractor;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.component.TwoByIndexGGRExtractor;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component.StandardTIDeterminer;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.StandardTrafficIndicatorsCreator;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component.TIOnCrossroadProcessor;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component.TIOnRoadProcessor;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component.helper.SimulationTimeStepGetter;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.sort.ByAngleEdgeSorter;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.SignalsConfigurationService;

@ExtendWith(MockitoExtension.class)
public class TrafficSignalsCorrectorTest {
  private final SignalsConfigurationService signalConfigService = Mockito.mock(SignalsConfigurationService.class);
  private final SimulationTimeStepGetter simulationTimeStepGetter = Mockito.mock(SimulationTimeStepGetter.class);
  private final TrafficSignalsCorrector corrector = new TrafficSignalsCorrector(
      new StandardGreenGroupsAggregator(
          new MixedTwoAngleIndexGGRExtractor(new TwoByIndexGGRExtractor(), new TwoByAngleGGRExtractor()),
          new ByAngleEdgeSorter()),
      new StandardTrafficIndicatorsCreator(
          List.of(
              new TIOnCrossroadProcessor(signalConfigService, simulationTimeStepGetter),
              new TIOnRoadProcessor(
                  new StandardTIDeterminer(),
                  new TIOnCrossroadProcessor(signalConfigService, simulationTimeStepGetter))
          ),
          new StandardTIDeterminer())
  );

  @Test
  public void happyPathCreatingTrafficSignalsSystem() {
    // given
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).tags(new HashMap<>(){{put("highway", "traffic_signals");}})
            .lat(50.0850637).lon(19.8911986).build());
    Node<JunctionData, WayData> nextNode1 = new Node<>("1",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0843471).lon(19.8911159).build());
    Node<JunctionData, WayData> nextNode2 = new Node<>("2",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0852816).lon(19.8912199).build());
    Node<JunctionData, WayData> nextNode3 = new Node<>("3",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0852747).lon(19.8913083).build());
    Edge<JunctionData, WayData> inEdge1 = new Edge<>("10",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> inEdge2 = new Edge<>("20",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> inEdge3 = new Edge<>("30",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("01",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge2 = new Edge<>("02",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge3 = new Edge<>("03",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());

    // when
    inEdge1.setSource(nextNode1);
    inEdge1.setTarget(crossroad);
    inEdge2.setSource(nextNode2);
    inEdge2.setTarget(crossroad);
    inEdge3.setSource(nextNode3);
    inEdge3.setTarget(crossroad);
    outEdge1.setSource(crossroad);
    outEdge1.setTarget(nextNode1);
    outEdge2.setSource(crossroad);
    outEdge2.setTarget(nextNode2);
    outEdge3.setSource(crossroad);
    outEdge3.setTarget(nextNode3);
    crossroad.getIncomingEdges().addAll(List.of(inEdge1, inEdge2, inEdge3));
    crossroad.getOutgoingEdges().addAll(List.of(outEdge1, outEdge2, outEdge3));
    nextNode1.getIncomingEdges().add(outEdge1);
    nextNode1.getOutgoingEdges().add(inEdge1);
    nextNode2.getIncomingEdges().add(outEdge2);
    nextNode2.getOutgoingEdges().add(inEdge2);
    nextNode3.getIncomingEdges().add(outEdge3);
    nextNode3.getOutgoingEdges().add(inEdge3);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(crossroad)
        .addNode(nextNode1)
        .addNode(nextNode2)
        .addNode(nextNode3)
        .addEdge(inEdge1)
        .addEdge(inEdge2)
        .addEdge(inEdge3)
        .addEdge(outEdge1)
        .addEdge(outEdge2)
        .addEdge(outEdge3)
        .build();
    Mockito.when(signalConfigService.getTimeForSpecificNode(Mockito.any())).thenReturn(0);
    Mockito.when(simulationTimeStepGetter.get()).thenReturn(1);
    corrector.correct(graph);

    // then
    Assertions.assertTrue(crossroad.getData().getSignalsControlCenter().isPresent());
    Assertions.assertTrue(inEdge1.getData().getTrafficIndicator().isPresent());
    Assertions.assertTrue(inEdge2.getData().getTrafficIndicator().isPresent());
    Assertions.assertTrue(inEdge3.getData().getTrafficIndicator().isPresent());
    Assertions.assertEquals(2, crossroad.getData().getSignalsControlCenter().get().getGreenColorGroups().size());
  }
}
