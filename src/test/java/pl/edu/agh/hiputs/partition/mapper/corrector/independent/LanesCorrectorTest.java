package pl.edu.agh.hiputs.partition.mapper.corrector.independent;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.lanes.StandardLanesCreator;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.OnBendSuccessorAllocator;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.OnCrossroadSuccessorAllocator;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.pairing.DefaultPairingIncomingWithOutgoings;
import pl.edu.agh.hiputs.partition.mapper.helper.service.oneway.StandardOsmAndRoundaboutOnewayProcessor;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.sort.ByAngleEdgeSorter;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.mapper.FixedAngleRangeTurnMapper;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.processor.StandardOsmTurnProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class LanesCorrectorTest {
  private final LanesCorrector corrector = new LanesCorrector(
      new StandardLanesCreator(new StandardOsmAndRoundaboutOnewayProcessor()),
      List.of(
          new OnBendSuccessorAllocator(new StandardOsmTurnProcessor()),
          new OnCrossroadSuccessorAllocator(
              new DefaultPairingIncomingWithOutgoings(),
              new StandardOsmTurnProcessor(),
              new FixedAngleRangeTurnMapper(),
              new ByAngleEdgeSorter()
          )
      )
  );

  @Test
  public void happyPathCreatingLanesAndAssigningSuccessors() {
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
            .tags(Collections.singletonMap("lanes", "4"))
            .build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("02",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge2 = new Edge<>("03",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());

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
    Graph<JunctionData, WayData> returned = corrector.correct(graph);

    // then
    Assertions.assertEquals(2, returned.getEdges().values().stream()
        .flatMap(edge -> edge.getData().getLanes().stream())
        .filter(lane -> !lane.getAvailableSuccessors().isEmpty())
        .count());
  }
}
