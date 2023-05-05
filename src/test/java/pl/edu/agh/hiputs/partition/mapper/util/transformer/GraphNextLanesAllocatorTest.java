package pl.edu.agh.hiputs.partition.mapper.util.transformer;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import pl.edu.agh.hiputs.partition.mapper.util.sort.ByAngleEdgeSorter;
import pl.edu.agh.hiputs.partition.mapper.util.successor.allocator.OnBendSuccessorAllocator;
import pl.edu.agh.hiputs.partition.mapper.util.successor.allocator.OnCrossroadSuccessorAllocator;
import pl.edu.agh.hiputs.partition.mapper.util.successor.pairing.DefaultPairingIncomingWithOutgoings;
import pl.edu.agh.hiputs.partition.mapper.util.turn.mapper.FixedAngleRangeTurnMapper;
import pl.edu.agh.hiputs.partition.mapper.util.turn.processor.StandardOsmTurnProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class GraphNextLanesAllocatorTest {
  private final GraphNextLanesAllocator allocator = new GraphNextLanesAllocator(
      List.of(
          new OnBendSuccessorAllocator(
              new StandardOsmTurnProcessor()
          ),
          new OnCrossroadSuccessorAllocator(
              new DefaultPairingIncomingWithOutgoings(), new StandardOsmTurnProcessor(),
              new FixedAngleRangeTurnMapper(), new ByAngleEdgeSorter()
          )
      )
  );

  @Test
  public void transformNoLanesInProvidedGraph() {
    // given
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).lat(50.0850637).lon(19.8911986).build());
    Node<JunctionData, WayData> startNode = new Node<>("1",
        JunctionData.builder().isCrossroad(false).lat(50.0843471).lon(19.8911159).build());
    Node<JunctionData, WayData> nextNode1 = new Node<>("2",
        JunctionData.builder().isCrossroad(false).lat(50.0852816).lon(19.8912199).build());
    Node<JunctionData, WayData> nextNode2 = new Node<>("3",
        JunctionData.builder().isCrossroad(false).lat(50.0852747).lon(19.8913083).build());
    Edge<JunctionData, WayData> inEdge = new Edge<>("10",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("02",
        WayData.builder().build());
    Edge<JunctionData, WayData> outEdge2 = new Edge<>("03",
        WayData.builder().build());

    // when
    inEdge.setSource(startNode);
    inEdge.setTarget(crossroad);
    outEdge1.setSource(crossroad);
    outEdge1.setTarget(nextNode1);
    outEdge2.setSource(crossroad);
    outEdge2.setTarget(nextNode2);
    crossroad.getIncomingEdges().add(inEdge);
    crossroad.getOutgoingEdges().addAll(List.of(outEdge1, outEdge2));
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(startNode)
        .addNode(crossroad)
        .addNode(nextNode1)
        .addNode(nextNode2)
        .addEdge(inEdge)
        .addEdge(outEdge1)
        .addEdge(outEdge2)
        .build();
    allocator.transform(graph);

    // then
    Assertions.assertEquals(0, graph.getEdges().values().stream()
        .flatMap(edge -> edge.getData().getLanes().stream())
        .filter(lane -> !lane.getAvailableSuccessors().isEmpty())
        .count());
  }

  @Test
  public void transformAllLanesInProvidedGraph() {
    // given
    LaneData inLane1 = LaneData.builder().build();
    LaneData inLane2 = LaneData.builder().build();
    LaneData outLane1 = LaneData.builder().build();
    LaneData outLane2 = LaneData.builder().build();
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).lat(50.0850637).lon(19.8911986).build());
    Node<JunctionData, WayData> startNode = new Node<>("1",
        JunctionData.builder().isCrossroad(false).lat(50.0843471).lon(19.8911159).build());
    Node<JunctionData, WayData> nextNode1 = new Node<>("2",
        JunctionData.builder().isCrossroad(false).lat(50.0852816).lon(19.8912199).build());
    Node<JunctionData, WayData> nextNode2 = new Node<>("3",
        JunctionData.builder().isCrossroad(false).lat(50.0852747).lon(19.8913083).build());
    Edge<JunctionData, WayData> inEdge = new Edge<>("10",
        WayData.builder()
            .lanes(List.of(inLane1, inLane2))
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("02",
        WayData.builder().lanes(List.of(outLane1)).build());
    Edge<JunctionData, WayData> outEdge2 = new Edge<>("03",
        WayData.builder().lanes(List.of(outLane2)).build());

    // when
    inEdge.setSource(startNode);
    inEdge.setTarget(crossroad);
    outEdge1.setSource(crossroad);
    outEdge1.setTarget(nextNode1);
    outEdge2.setSource(crossroad);
    outEdge2.setTarget(nextNode2);
    crossroad.getIncomingEdges().add(inEdge);
    crossroad.getOutgoingEdges().addAll(List.of(outEdge1, outEdge2));
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(startNode)
        .addNode(crossroad)
        .addNode(nextNode1)
        .addNode(nextNode2)
        .addEdge(inEdge)
        .addEdge(outEdge1)
        .addEdge(outEdge2)
        .build();
    allocator.transform(graph);

    // then
    Assertions.assertEquals(2, graph.getEdges().values().stream()
        .flatMap(edge -> edge.getData().getLanes().stream())
        .filter(lane -> !lane.getAvailableSuccessors().isEmpty())
        .count());
  }
}
