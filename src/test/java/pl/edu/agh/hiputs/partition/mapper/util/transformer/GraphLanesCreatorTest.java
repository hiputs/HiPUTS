package pl.edu.agh.hiputs.partition.mapper.util.transformer;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.edu.agh.hiputs.partition.mapper.util.oneway.StandardOsmAndRoundaboutOnewayProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class GraphLanesCreatorTest {
  private final GraphLanesCreator creator = new GraphLanesCreator(new StandardOsmAndRoundaboutOnewayProcessor());

  @ParameterizedTest
  @MethodSource("provideParamsForLaneNoChecking")
  public void checkNumberOfLanes(Map<String, String> tags, boolean oppositeMeaning, int resultNumber) {
    // given
    Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder().build());
    Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge = new Edge<>("12", WayData.builder()
        .tags(tags)
        .tagsInOppositeMeaning(oppositeMeaning)
        .build());
    edge.setSource(node1);
    edge.setTarget(node2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(node1)
        .addNode(node2)
        .addEdge(edge)
        .build();

    // when
    creator.transform(graph);

    // then
    Assertions.assertEquals(resultNumber, edge.getData().getLanes().size());
  }

  private static Stream<Arguments> provideParamsForLaneNoChecking() {
    return Stream.of(
        Arguments.of(Map.of(), false, 1),
        Arguments.of(Map.of("lanes", "2"), false, 1),
        Arguments.of(Map.of("lanes", "2", "oneway", "yes"), false, 2),
        Arguments.of(Map.of("lanes", "3", "lanes:forward", "1"), false, 1),
        Arguments.of(Map.of("lanes", "3", "lanes:forward", "1"), true, 2),
        Arguments.of(Map.of("lanes", "3", "lanes:backward", "1"), false, 2),
        Arguments.of(Map.of("lanes", "3", "lanes:backward", "1"), true, 1),
        Arguments.of(Map.of("lanes", "3", "lanes:forward", "2", "lanes:backward", "1"), false, 2),
        Arguments.of(Map.of("lanes", "3", "lanes:forward", "2", "lanes:backward", "1"), true, 1),
        Arguments.of(Map.of("lanes", "3",
            "lanes:forward", "2", "lanes:backward", "1", "lanes:both_ways", "1"), false, 3),
        Arguments.of(Map.of("lanes", "3",
            "lanes:forward", "2", "lanes:backward", "1", "lanes:both_ways", "1"), true, 2)
    );
  }
}
