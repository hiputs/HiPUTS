package pl.edu.agh.hiputs.partition.mapper.transformer;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.edu.agh.hiputs.partition.mapper.helper.service.crossroad.StandardCrossroadDeterminer;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class GraphCrossroadDeterminerTest {

  private final static Node<JunctionData, WayData> center = new Node<>("0", JunctionData.builder().build());
  private final GraphCrossroadDeterminer determiner = new GraphCrossroadDeterminer(new StandardCrossroadDeterminer());

  @AfterEach
  public void cleanUp() {
    center.getIncomingEdges().clear();
    center.getOutgoingEdges().clear();
  }

  @ParameterizedTest
  @MethodSource("provideParamsForDifferentNumberOfEdgesTest")
  public void testByDifferentNumberOfEdges(List<Edge<JunctionData, WayData>> incomingEdges,
      List<Edge<JunctionData, WayData>> outgoingEdges, boolean result) {
    // given
    center.getIncomingEdges().addAll(incomingEdges);
    center.getOutgoingEdges().addAll(outgoingEdges);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(center).build();

    // when
    determiner.transform(graph);

    // then
    Assertions.assertEquals(result, center.getData().isCrossroad());
  }

  private static Stream<Arguments> provideParamsForDifferentNumberOfEdgesTest() {
    Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder().build());
    Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder().build());
    Node<JunctionData, WayData> node3 = new Node<>("3", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("11", WayData.builder().build());
    Edge<JunctionData, WayData> edge1Back = new Edge<>("111", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("33", WayData.builder().build());
    Edge<JunctionData, WayData> edge2Back = new Edge<>("333", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("44", WayData.builder().build());

    edge1.setSource(node1);
    edge1.setTarget(center);
    edge2.setSource(center);
    edge2.setTarget(node2);
    edge3.setSource(center);
    edge3.setTarget(node3);
    edge1Back.setSource(center);
    edge1Back.setTarget(node1);
    edge2Back.setSource(node2);
    edge2Back.setTarget(center);

    return Stream.of(Arguments.of(List.of(edge1, edge2Back), List.of(edge2, edge3), true),
        Arguments.of(List.of(edge1, edge2Back), List.of(edge2, edge1Back), false));
  }
}
