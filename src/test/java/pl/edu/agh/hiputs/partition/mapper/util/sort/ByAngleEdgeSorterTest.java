package pl.edu.agh.hiputs.partition.mapper.util.sort;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
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

public class ByAngleEdgeSorterTest {
  private final ByAngleEdgeSorter sorter = new ByAngleEdgeSorter();

  private final static Node<JunctionData, WayData> crossroad = new Node<>("0",
      JunctionData.builder().isCrossroad(true).lat(50.0850637).lon(19.8911986).build());
  private final static Node<JunctionData, WayData> firstNode = new Node<>("1",
      JunctionData.builder().isCrossroad(false).lat(50.0843471).lon(19.8911159).build());
  private final static Node<JunctionData, WayData> secondNode = new Node<>("2",
      JunctionData.builder().isCrossroad(false).lat(50.0852816).lon(19.8912199).build());
  private final static Node<JunctionData, WayData> thirdNode = new Node<>("3",
      JunctionData.builder().isCrossroad(false).lat(50.0852747).lon(19.8913083).build());

  private final static Edge<JunctionData, WayData> firstEdge = new Edge<>("01", WayData.builder().build());
  private final static Edge<JunctionData, WayData> secondEdge = new Edge<>("02", WayData.builder().build());
  private final static Edge<JunctionData, WayData> thirdEdge = new Edge<>("03", WayData.builder().build());

  private final static Edge<JunctionData, WayData> refEdge = new Edge<>("10", WayData.builder().build());

  private final static Edge<JunctionData, WayData> reverseFirstEdge = new Edge<>("10", WayData.builder().build());
  private final static Edge<JunctionData, WayData> reverseSecondEdge = new Edge<>("20", WayData.builder().build());
  private final static Edge<JunctionData, WayData> reverseThirdEdge = new Edge<>("30", WayData.builder().build());

  @BeforeAll
  public static void initAll() {
    refEdge.setSource(firstNode);
    refEdge.setTarget(crossroad);
    firstEdge.setSource(crossroad);
    firstEdge.setTarget(firstNode);
    secondEdge.setSource(crossroad);
    secondEdge.setTarget(secondNode);
    thirdEdge.setSource(crossroad);
    thirdEdge.setTarget(thirdNode);
    reverseFirstEdge.setSource(firstNode);
    reverseFirstEdge.setTarget(crossroad);
    reverseSecondEdge.setSource(secondNode);
    reverseSecondEdge.setTarget(crossroad);
    reverseThirdEdge.setSource(thirdNode);
    reverseThirdEdge.setTarget(crossroad);
  }

  @ParameterizedTest
  @MethodSource("provideParamsForMultipleEdgesSorting")
  public void sortEdges(
      List<Edge<JunctionData, WayData>> edges,
      List<Edge<JunctionData, WayData>> sorted,
      Function<Edge<JunctionData, WayData>, JunctionData> nodeGetter) {
    // given

    // when
    List<Edge<JunctionData, WayData>> sortedEdges = sorter.getSorted(edges, refEdge, nodeGetter);

    // then
    Assertions.assertEquals(sorted.size(), sortedEdges.size());
    IntStream.range(0, sorted.size())
        .forEach(index -> Assertions.assertEquals(sorted.get(index), sortedEdges.get(index)));
  }

  private static Stream<Arguments> provideParamsForMultipleEdgesSorting() {
    Function<Edge<JunctionData, WayData>, JunctionData> targetGetter = edge -> edge.getTarget().getData();
    Function<Edge<JunctionData, WayData>, JunctionData> sourceGetter = edge -> edge.getSource().getData();

    return Stream.of(
        Arguments.of(List.of(firstEdge, secondEdge, thirdEdge), List.of(firstEdge, secondEdge, thirdEdge), targetGetter),
        Arguments.of(List.of(secondEdge, thirdEdge, firstEdge), List.of(firstEdge, secondEdge, thirdEdge), targetGetter),
        Arguments.of(List.of(firstEdge), List.of(firstEdge), targetGetter),
        Arguments.of(List.of(), List.of(), targetGetter),
        Arguments.of(List.of(reverseFirstEdge, reverseSecondEdge, reverseThirdEdge),
            List.of(reverseFirstEdge, reverseSecondEdge, reverseThirdEdge), sourceGetter),
        Arguments.of(List.of(reverseSecondEdge, reverseThirdEdge, reverseFirstEdge),
            List.of(reverseFirstEdge, reverseSecondEdge, reverseThirdEdge), sourceGetter),
        Arguments.of(List.of(reverseFirstEdge), List.of(reverseFirstEdge), sourceGetter),
        Arguments.of(List.of(), List.of(), sourceGetter)
    );
  }
}
