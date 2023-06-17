package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component.StandardTIDeterminer;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component.TIOnCrossroadProcessor;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component.TIOnRoadProcessor;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component.helper.SimulationTimeStepGetter;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.SignalsConfigurationService;

@ExtendWith(MockitoExtension.class)
public class StandardTrafficIndicatorsCreatorTest {
  private final SignalsConfigurationService signalConfigService = Mockito.mock(SignalsConfigurationService.class);
  private final SimulationTimeStepGetter simulationTimeStepGetter = Mockito.mock(SimulationTimeStepGetter.class);
  private final StandardTrafficIndicatorsCreator creator = new StandardTrafficIndicatorsCreator(
      List.of(
          new TIOnCrossroadProcessor(signalConfigService, simulationTimeStepGetter),
          new TIOnRoadProcessor(
              new StandardTIDeterminer(),
              new TIOnCrossroadProcessor(signalConfigService, simulationTimeStepGetter))
      ),
      new StandardTIDeterminer());

  @Test
  public void transformNoTI() {
    // given
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).lat(50.0850637).tags(new HashMap<>()).lon(19.8911986).build());
    Node<JunctionData, WayData> firstNode = new Node<>("1",
        JunctionData.builder().isCrossroad(false).lat(50.0843471).tags(new HashMap<>()).lon(19.8911159).build());
    Node<JunctionData, WayData> secondNode = new Node<>("2",
        JunctionData.builder().isCrossroad(false).lat(50.0852816).tags(new HashMap<>()).lon(19.8912199).build());
    Node<JunctionData, WayData> thirdNode = new Node<>("3",
        JunctionData.builder().isCrossroad(false).lat(50.0852747).tags(new HashMap<>()).lon(19.8913083).build());

    Edge<JunctionData, WayData> inEdge1 = new Edge<>("10", WayData.builder().build());
    Edge<JunctionData, WayData> inEdge2 = new Edge<>("20", WayData.builder().build());
    Edge<JunctionData, WayData> inEdge3 = new Edge<>("30", WayData.builder().build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("01", WayData.builder().build());
    Edge<JunctionData, WayData> outEdge2 = new Edge<>("02", WayData.builder().build());
    Edge<JunctionData, WayData> outEdge3 = new Edge<>("03", WayData.builder().build());

    // when
    inEdge1.setSource(firstNode);
    inEdge1.setTarget(crossroad);
    inEdge2.setSource(secondNode);
    inEdge2.setTarget(crossroad);
    inEdge3.setSource(thirdNode);
    inEdge3.setTarget(crossroad);
    outEdge1.setSource(crossroad);
    outEdge1.setTarget(firstNode);
    outEdge2.setSource(crossroad);
    outEdge2.setTarget(secondNode);
    outEdge3.setSource(crossroad);
    outEdge3.setTarget(thirdNode);

    crossroad.getData().getTags().clear();
    crossroad.getIncomingEdges().addAll(List.of(inEdge1, inEdge2, inEdge3));
    crossroad.getOutgoingEdges().addAll(List.of(outEdge1, outEdge2, outEdge3));
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(crossroad)
        .addNode(firstNode)
        .addNode(secondNode)
        .addNode(thirdNode)
        .addEdge(inEdge1)
        .addEdge(inEdge2)
        .addEdge(inEdge3)
        .addEdge(outEdge1)
        .addEdge(outEdge2)
        .addEdge(outEdge3)
        .build();
    creator.createTIsAndMarkCrossroads(graph);

    // then
    Assertions.assertTrue(graph.getNodes().values().stream()
        .noneMatch(node -> node.getData().getSignalsControlCenter().isPresent()));
    Assertions.assertTrue(graph.getEdges().values().stream()
        .noneMatch(edge -> edge.getData().getTrafficIndicator().isPresent()));
  }

  @Test
  public void transformTI() {
    // given
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).lat(50.0850637).tags(new HashMap<>()).lon(19.8911986).build());
    Node<JunctionData, WayData> firstNode = new Node<>("1",
        JunctionData.builder().isCrossroad(false).lat(50.0843471).tags(new HashMap<>()).lon(19.8911159).build());
    Node<JunctionData, WayData> secondNode = new Node<>("2",
        JunctionData.builder().isCrossroad(false).lat(50.0852816).tags(new HashMap<>()).lon(19.8912199).build());
    Node<JunctionData, WayData> thirdNode = new Node<>("3",
        JunctionData.builder().isCrossroad(false).lat(50.0852747).tags(new HashMap<>()).lon(19.8913083).build());

    Edge<JunctionData, WayData> inEdge1 = new Edge<>("10", WayData.builder().build());
    Edge<JunctionData, WayData> inEdge2 = new Edge<>("20", WayData.builder().build());
    Edge<JunctionData, WayData> inEdge3 = new Edge<>("30", WayData.builder().build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("01", WayData.builder().build());
    Edge<JunctionData, WayData> outEdge2 = new Edge<>("02", WayData.builder().build());
    Edge<JunctionData, WayData> outEdge3 = new Edge<>("03", WayData.builder().build());

    // when
    inEdge1.setSource(firstNode);
    inEdge1.setTarget(crossroad);
    inEdge2.setSource(secondNode);
    inEdge2.setTarget(crossroad);
    inEdge3.setSource(thirdNode);
    inEdge3.setTarget(crossroad);
    outEdge1.setSource(crossroad);
    outEdge1.setTarget(firstNode);
    outEdge2.setSource(crossroad);
    outEdge2.setTarget(secondNode);
    outEdge3.setSource(crossroad);
    outEdge3.setTarget(thirdNode);

    crossroad.getData().getTags().clear();
    crossroad.getIncomingEdges().addAll(List.of(inEdge1, inEdge2, inEdge3));
    crossroad.getOutgoingEdges().addAll(List.of(outEdge1, outEdge2, outEdge3));
    crossroad.getData().getTags().put("highway", "traffic_signals");
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(crossroad)
        .addNode(firstNode)
        .addNode(secondNode)
        .addNode(thirdNode)
        .addEdge(inEdge1)
        .addEdge(inEdge2)
        .addEdge(inEdge3)
        .addEdge(outEdge1)
        .addEdge(outEdge2)
        .addEdge(outEdge3)
        .build();

    Mockito.when(signalConfigService.getTimeForSpecificNode(Mockito.any())).thenReturn(0);
    Mockito.when(simulationTimeStepGetter.get()).thenReturn(1);
    creator.createTIsAndMarkCrossroads(graph);

    // then
    Assertions.assertTrue(graph.getNodes().values().stream()
        .anyMatch(node -> node.getData().getSignalsControlCenter().isPresent()));
    Assertions.assertTrue(graph.getEdges().values().stream()
        .anyMatch(edge -> edge.getData().getTrafficIndicator().isPresent()));
  }
}
