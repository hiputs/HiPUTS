package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.component;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public class TwoByIndexGGRExtractorTest {
  private final TwoByIndexGGRExtractor extractor = new TwoByIndexGGRExtractor();

  @Test
  public void zeroNumberOfEdgesToExtract() {
    // given

    // when

    // then
    Assertions.assertTrue(extractor.extract(Collections.emptyList()).isEmpty());
  }

  @Test
  public void twoNumberOfEdgesToExtract() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());

    // when
    List<List<Edge<JunctionData, WayData>>> result = extractor.extract(List.of(edge1, edge2));

    // then
    Assertions.assertEquals(2, result.size());
    result.forEach(group -> Assertions.assertEquals(1, group.size()));
  }

  @Test
  public void evenNumberOfEdgesToExtract() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().build());

    // when
    List<List<Edge<JunctionData, WayData>>> result = extractor.extract(List.of(edge1, edge2, edge3, edge4));

    // then
    Assertions.assertEquals(2, result.size());
    result.forEach(group -> Assertions.assertEquals(2, group.size()));
  }

  @Test
  public void oddNumberOfEdgesToExtract() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().build());

    // when
    List<List<Edge<JunctionData, WayData>>> result = extractor.extract(List.of(edge1, edge2, edge3, edge4, edge5));

    // then
    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals(2, result.get(0).size());
    Assertions.assertEquals(2, result.get(1).size());
    Assertions.assertEquals(1, result.get(2).size());
  }

  @Test
  public void biggerNumberOfEdgesToSplit() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().build());

    // when
    List<List<Edge<JunctionData, WayData>>> result = extractor.split(List.of(edge1, edge2, edge3, edge4, edge5), 4);

    // then
    Assertions.assertEquals(4, result.size());
    Assertions.assertEquals(2, result.get(0).size());
    Assertions.assertEquals(1, result.get(1).size());
    Assertions.assertEquals(1, result.get(2).size());
    Assertions.assertEquals(1, result.get(3).size());
  }
}
