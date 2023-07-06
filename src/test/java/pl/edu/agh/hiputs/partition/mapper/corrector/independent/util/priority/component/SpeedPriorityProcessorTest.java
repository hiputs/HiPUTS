package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority.component;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public class SpeedPriorityProcessorTest {
  private final SpeedPriorityProcessor priorityProcessor = new SpeedPriorityProcessor();

  @Test
  public void emptyList() {
    // given

    // when

    // then
    Assertions.assertFalse(priorityProcessor.compareRoads(List.of()).isPresent());
  }

  @Test
  public void singleEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().maxSpeed(10).build());

    // when

    // then
    Optional<Edge<JunctionData, WayData>> resultEdge = priorityProcessor.compareRoads(List.of(edge1));
    Assertions.assertFalse(resultEdge.isPresent());
  }

  @Test
  public void twoEdgesWithTheSameValue() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().maxSpeed(10).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().maxSpeed(10).build());

    // when

    // then
    Optional<Edge<JunctionData, WayData>> resultEdge = priorityProcessor.compareRoads(List.of(edge1, edge2));
    Assertions.assertFalse(resultEdge.isPresent());
  }

  @Test
  public void twoEdgesWithDifferentValue() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().maxSpeed(10).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().maxSpeed(20).build());

    // when

    // then
    Optional<Edge<JunctionData, WayData>> resultEdge = priorityProcessor.compareRoads(List.of(edge1, edge2));
    Assertions.assertTrue(resultEdge.isPresent());
    Assertions.assertEquals(edge2, resultEdge.get());
  }
}
