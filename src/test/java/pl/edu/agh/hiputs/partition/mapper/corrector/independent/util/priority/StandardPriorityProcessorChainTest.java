package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority.component.SpeedPriorityProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public class StandardPriorityProcessorChainTest {

  @Test
  public void emptyListEmptyProcessors() {
    // given

    // when
    StandardPriorityProcessorChain chain = new StandardPriorityProcessorChain(List.of());

    // then
    Edge<JunctionData, WayData> resultEdge = chain.getTopPriorityRoad(List.of());
    Assertions.assertNull(resultEdge);
  }

  @Test
  public void emptyProcessors() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());

    // when
    StandardPriorityProcessorChain chain = new StandardPriorityProcessorChain(List.of());

    // then
    Edge<JunctionData, WayData> resultEdge = chain.getTopPriorityRoad(List.of(edge1, edge2));
    Assertions.assertNull(resultEdge);
  }

  @Test
  public void emptyList() {
    // given

    // when
    StandardPriorityProcessorChain chain = new StandardPriorityProcessorChain(List.of(new SpeedPriorityProcessor()));

    // then
    Edge<JunctionData, WayData> resultEdge = chain.getTopPriorityRoad(List.of());
    Assertions.assertNull(resultEdge);
  }

  @Test
  public void happyPath() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().maxSpeed(10).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().maxSpeed(20).build());

    // when
    StandardPriorityProcessorChain chain = new StandardPriorityProcessorChain(List.of(new SpeedPriorityProcessor()));

    // then
    Edge<JunctionData, WayData> resultEdge = chain.getTopPriorityRoad(List.of(edge1, edge2));
    Assertions.assertEquals(edge2, resultEdge);
  }
}
