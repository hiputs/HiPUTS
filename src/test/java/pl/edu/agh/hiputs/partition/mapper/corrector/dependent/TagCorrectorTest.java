package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class TagCorrectorTest {

  private final static Edge<JunctionData, WayData> edge1 =
      new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
  private final static Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
    put("name", "Komputerowa");
    put("maxspeed", "70");
  }}).build());
  private final static Edge<JunctionData, WayData> edge3 =
      new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
  private final static Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().tags(new HashMap<>() {{
    put("name", "Laptopowa");
    put("maxspeed", "50");
  }}).build());
  private final static Edge<JunctionData, WayData> edge5 =
      new Edge<>("5", WayData.builder().tags(new HashMap<>()).build());
  private final static Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
  private final static Node<JunctionData, WayData> nodeB =
      new Node<>("B", JunctionData.builder().tags(new HashMap<>() {{
        put("highway", "traffic_signals");
      }}).build());
  private final static Node<JunctionData, WayData> nodeC =
      new Node<>("C", JunctionData.builder().tags(new HashMap<>()).build());
  private final static Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
  private final static Node<JunctionData, WayData> nodeE = new Node<>("E", JunctionData.builder().build());
  private final static Node<JunctionData, WayData> nodeF = new Node<>("F", JunctionData.builder().build());

  @BeforeAll
  public static void init() {
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeD);
    edge4.setSource(nodeD);
    edge4.setTarget(nodeE);
    edge5.setSource(nodeE);
    edge5.setTarget(nodeF);

    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    nodeD.getIncomingEdges().add(edge3);
    nodeD.getOutgoingEdges().add(edge4);
    nodeE.getIncomingEdges().add(edge4);
    nodeE.getOutgoingEdges().add(edge5);
    nodeF.getIncomingEdges().add(edge5);
  }

  @ParameterizedTest
  @MethodSource("provideParamsForEdgesCorrectionTest")
  public void edgesCorrectionTest(String tag, List<Edge<JunctionData, WayData>> edges, String result) {
    // given
    TagCorrector tagCorrector = new TagCorrector(List.of(Pair.of(tag, edges)), List.of());

    // when
    tagCorrector.correct(null);

    // then
    Assertions.assertEquals(result, edges.get(0).getData().getTags().get(tag));
  }

  private static Stream<Arguments> provideParamsForEdgesCorrectionTest() {
    return Stream.of(Arguments.of("maxspeed", List.of(edge1), "70"), Arguments.of("maxspeed", List.of(edge3), "60"),
        Arguments.of("maxspeed", List.of(edge5), "50"));
  }

  @Test
  public void trafficSignalsRemovalWhenNotExists() {
    // given
    TagCorrector tagCorrector = new TagCorrector(List.of(), List.of(Pair.of("traffic_signals", List.of(nodeC))));

    // when
    boolean statusBeforeC = nodeC.getData().getTags().containsValue("traffic_signals");
    tagCorrector.correct(null);

    // then
    Assertions.assertEquals(statusBeforeC, nodeC.getData().getTags().containsValue("traffic_signals"));
  }

  @Test
  public void trafficSignalsRemovalWhenExists() {
    // given
    TagCorrector tagCorrector = new TagCorrector(List.of(), List.of(Pair.of("traffic_signals", List.of(nodeB))));

    // when
    boolean statusBeforeB = nodeB.getData().getTags().containsValue("traffic_signals");
    tagCorrector.correct(null);

    // then
    Assertions.assertEquals(!statusBeforeB, nodeB.getData().getTags().containsValue("traffic_signals"));
  }
}
