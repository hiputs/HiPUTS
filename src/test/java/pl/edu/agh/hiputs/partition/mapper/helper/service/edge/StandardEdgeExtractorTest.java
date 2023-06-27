package pl.edu.agh.hiputs.partition.mapper.helper.service.edge;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class StandardEdgeExtractorTest {
  private final StandardEdgeExtractor extractor = new StandardEdgeExtractor();

  private final static Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
      .tags(Map.of("name", "Walking")).build());
  private final static Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder()
      .tags(Map.of("name", "Walking")).build());
  private final static Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder()
      .tags(Map.of("name", "Walking")).build());
  private final static Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder()
      .tags(Map.of("name", "Walking")).build());
  private final static Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder()
      .tags(Map.of("name", "Walking")).build());
  private final static Edge<JunctionData, WayData> edge6 = new Edge<>("6", WayData.builder()
      .tags(Map.of("name", "Walking")).build());
  private final static Edge<JunctionData, WayData> edge7 = new Edge<>("7", WayData.builder()
      .tags(Map.of("name", "Walking")).build());
  private final static Edge<JunctionData, WayData> edge8 = new Edge<>("8", WayData.builder()
      .tags(Map.of("addr:country", "PL")).build());
  private final static Edge<JunctionData, WayData> edge9 = new Edge<>("9", WayData.builder()
      .tags(Map.of("addr:country", "PL")).build());
  private final static Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
  private final static Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
  private final static Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
  private final static Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
  private final static Node<JunctionData, WayData> nodeE = new Node<>("E", JunctionData.builder().build());
  private final static Node<JunctionData, WayData> nodeF = new Node<>("F", JunctionData.builder().build());

  @BeforeAll
  public static void init() {
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeD);
    edge3.setTarget(nodeE);
    edge4.setSource(nodeE);
    edge4.setTarget(nodeF);
    edge5.setSource(nodeE);
    edge5.setTarget(nodeD);
    edge6.setSource(nodeD);
    edge6.setTarget(nodeC);
    edge7.setSource(nodeC);
    edge7.setTarget(nodeB);
    edge8.setSource(nodeC);
    edge8.setTarget(nodeD);
    edge9.setSource(nodeB);
    edge9.setTarget(nodeA);

    nodeA.getOutgoingEdges().add(edge1);
    nodeA.getIncomingEdges().add(edge9);
    nodeB.getOutgoingEdges().addAll(List.of(edge2, edge9));
    nodeB.getIncomingEdges().addAll(List.of(edge1, edge7));
    nodeC.getOutgoingEdges().addAll(List.of(edge7, edge8));
    nodeC.getIncomingEdges().addAll(List.of(edge2, edge6));
    nodeD.getOutgoingEdges().addAll(List.of(edge3, edge6));
    nodeD.getIncomingEdges().addAll(List.of(edge5, edge8));
    nodeE.getOutgoingEdges().addAll(List.of(edge4, edge5));
    nodeE.getIncomingEdges().add(edge3);
    nodeF.getIncomingEdges().add(edge4);
  }

  @ParameterizedTest
  @MethodSource("provideParamsForPredecessorTest")
  public void predecessorTest(Edge<JunctionData, WayData> edge, String key, boolean result) {
    // given

    // when

    // then
    Assertions.assertEquals(result, extractor.getPredecessorWithKey(edge, key).isPresent());
  }

  private static Stream<Arguments> provideParamsForPredecessorTest() {
    return Stream.of(
        Arguments.of(edge1, "name", false),
        Arguments.of(edge2, "name", true),
        Arguments.of(edge3, "name", false),
        Arguments.of(edge3, "addr:country", true),
        Arguments.of(edge4, "name", true)
    );
  }

  @ParameterizedTest
  @MethodSource("provideParamsForSuccessorTest")
  public void successorTest(Edge<JunctionData, WayData> edge, String key, boolean result) {
    // given

    // when

    // then
    Assertions.assertEquals(result, extractor.getSuccessorWithKey(edge, key).isPresent());
  }

  private static Stream<Arguments> provideParamsForSuccessorTest() {
    return Stream.of(
        Arguments.of(edge4, "name", false),
        Arguments.of(edge5, "name", true),
        Arguments.of(edge6, "name", true),
        Arguments.of(edge7, "name", false),
        Arguments.of(edge7, "addr:country", true)
    );
  }
}
