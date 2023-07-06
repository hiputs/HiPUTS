package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority.component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@ExtendWith(MockitoExtension.class)
public class TypePriorityProcessorTest {
  private final ModelConfigurationService modelConfigService = Mockito.mock(ModelConfigurationService.class);
  private final TypePriorityProcessor priorityProcessor = new TypePriorityProcessor(modelConfigService);
  private final Map<String, Integer> priorities = Map.of(
      "primary", 2,
      "tertiary", 1
  );

  @Test
  public void emptyList() {
    // given

    // when

    // then
    Assertions.assertFalse(priorityProcessor.compareRoads(List.of()).isPresent());
  }

  @Test
  public void singleEdgeWithTag() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(Map.of("highway", "primary")).build());

    // when
    Mockito.when(modelConfigService.getWayTypesPriority()).thenReturn(priorities);

    // then
    Optional<Edge<JunctionData, WayData>> resultEdge = priorityProcessor.compareRoads(List.of(edge1));
    Assertions.assertFalse(resultEdge.isPresent());
  }

  @Test
  public void singleEdgeWithoutTag() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(Map.of()).build());

    // when

    // then
    Optional<Edge<JunctionData, WayData>> resultEdge = priorityProcessor.compareRoads(List.of(edge1));
    Assertions.assertFalse(resultEdge.isPresent());
  }

  @Test
  public void twoEdgesWithTheSameValue() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(Map.of("highway", "primary")).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder()
        .tags(Map.of("highway", "primary")).build());

    // when
    Mockito.when(modelConfigService.getWayTypesPriority()).thenReturn(priorities);

    // then
    Optional<Edge<JunctionData, WayData>> resultEdge = priorityProcessor.compareRoads(List.of(edge1, edge2));
    Assertions.assertFalse(resultEdge.isPresent());
  }

  @Test
  public void twoEdgesWithDifferentValue() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(Map.of("highway", "tertiary")).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder()
        .tags(Map.of("highway", "primary")).build());

    // when
    Mockito.when(modelConfigService.getWayTypesPriority()).thenReturn(priorities);

    // then
    Optional<Edge<JunctionData, WayData>> resultEdge = priorityProcessor.compareRoads(List.of(edge1, edge2));
    Assertions.assertTrue(resultEdge.isPresent());
    Assertions.assertEquals(edge2, resultEdge.get());
  }

  @Test
  public void twoEdgesAndOneValueNotExists() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(Map.of("highway", "tertiary")).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder()
        .tags(Map.of()).build());

    // when
    Mockito.when(modelConfigService.getWayTypesPriority()).thenReturn(priorities);

    // then
    Optional<Edge<JunctionData, WayData>> resultEdge = priorityProcessor.compareRoads(List.of(edge1, edge2));
    Assertions.assertTrue(resultEdge.isPresent());
    Assertions.assertEquals(edge1, resultEdge.get());
  }
}
