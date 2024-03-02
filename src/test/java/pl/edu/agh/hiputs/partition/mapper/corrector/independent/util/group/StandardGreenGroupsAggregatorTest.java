package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.component.TwoByIndexGGRExtractor;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.sort.ByAngleEdgeSorter;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.lights.control.StandardSignalsControlCenter;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicator;

public class StandardGreenGroupsAggregatorTest {

  private final StandardGreenGroupsAggregator aggregator = new StandardGreenGroupsAggregator(
      new TwoByIndexGGRExtractor(), new ByAngleEdgeSorter()
  );

  private final static Node<JunctionData, WayData> crossroad = new Node<>("0",
      JunctionData.builder().isCrossroad(true).lat(50.0850637).tags(new HashMap<>()).lon(19.8911986).build());
  private final static Node<JunctionData, WayData> firstNode = new Node<>("1",
      JunctionData.builder().isCrossroad(false).lat(50.0843471).tags(new HashMap<>()).lon(19.8911159).build());
  private final static Node<JunctionData, WayData> secondNode = new Node<>("2",
      JunctionData.builder().isCrossroad(false).lat(50.0852816).tags(new HashMap<>()).lon(19.8912199).build());
  private final static Node<JunctionData, WayData> thirdNode = new Node<>("3",
      JunctionData.builder().isCrossroad(false).lat(50.0852747).tags(new HashMap<>()).lon(19.8913083).build());

  private final static Edge<JunctionData, WayData> inEdge1 = new Edge<>("10", WayData.builder().build());
  private final static Edge<JunctionData, WayData> inEdge2 = new Edge<>("20", WayData.builder().build());
  private final static Edge<JunctionData, WayData> inEdge3 = new Edge<>("30", WayData.builder().build());
  private final static Edge<JunctionData, WayData> outEdge1 = new Edge<>("01", WayData.builder().build());
  private final static Edge<JunctionData, WayData> outEdge2 = new Edge<>("02", WayData.builder().build());
  private final static Edge<JunctionData, WayData> outEdge3 = new Edge<>("03", WayData.builder().build());

  @BeforeAll
  public static void init() {
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

    crossroad.getIncomingEdges().addAll(List.of(inEdge1, inEdge2, inEdge3));
    crossroad.getOutgoingEdges().addAll(List.of(outEdge1, outEdge2, outEdge3));
  }

  @Test
  public void transformNoGreenGroups() {
    // given

    // when
    crossroad.getData().setSignalsControlCenter(Optional.empty());
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
    aggregator.findAndAggregate(graph);

    // then
    Assertions.assertTrue(graph.getNodes().values().stream()
        .noneMatch(node -> node.getData().getSignalsControlCenter().isPresent()));
  }

  @Test
  public void transformGreenGroups() {
    // given

    // when
    crossroad.getData().setSignalsControlCenter(Optional.of(new StandardSignalsControlCenter(0)));
    inEdge1.getData().setTrafficIndicator(Optional.of(new TrafficIndicator()));
    inEdge2.getData().setTrafficIndicator(Optional.of(new TrafficIndicator()));
    inEdge3.getData().setTrafficIndicator(Optional.of(new TrafficIndicator()));
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
    aggregator.findAndAggregate(graph);

    // then
    Assertions.assertTrue(graph.getNodes().values().stream()
        .anyMatch(node -> node.getData().getSignalsControlCenter().isPresent()));
    Assertions.assertTrue(graph.getNodes().values().stream()
        .filter(node -> node.getData().getSignalsControlCenter().isPresent())
        .noneMatch(node -> node.getData().getSignalsControlCenter().get().getGreenColorGroups().isEmpty()));
  }
}
