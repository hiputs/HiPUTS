package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.component;

import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public class MixedTwoAngleIndexGGRExtractorTest {
  @Test
  public void extractingZeroEdges() {
    // given
    MixedTwoAngleIndexGGRExtractor extractor = new MixedTwoAngleIndexGGRExtractor(
        new TwoByIndexGGRExtractor(), new TwoByAngleGGRExtractor()
    );

    // when
    List<List<Edge<JunctionData, WayData>>> resultGroups = extractor.extract(List.of());

    // then
    Assertions.assertEquals(0, resultGroups.size());
  }

  @Test
  public void extractingTwoEdges() {
    // given
    MixedTwoAngleIndexGGRExtractor extractor = new MixedTwoAngleIndexGGRExtractor(
        new TwoByIndexGGRExtractor(), new TwoByAngleGGRExtractor()
    );
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());

    // when
    List<List<Edge<JunctionData, WayData>>> resultGroups = extractor.extract(List.of(edge1, edge2));

    // then
    Assertions.assertEquals(2, resultGroups.size());
    Assertions.assertEquals(1, resultGroups.get(0).size());
    Assertions.assertEquals(1, resultGroups.get(1).size());
  }

  @Test
  public void extractingThreeEdgesWithSplitPointCloser() {
    // given
    TwoByAngleGGRExtractor angleExtractor = Mockito.mock(TwoByAngleGGRExtractor.class);
    TwoByIndexGGRExtractor indexExtractor = Mockito.mock(TwoByIndexGGRExtractor.class);
    MixedTwoAngleIndexGGRExtractor extractor = new MixedTwoAngleIndexGGRExtractor(indexExtractor, angleExtractor);
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());

    // when
    Mockito.when(angleExtractor.create(Mockito.any(), Mockito.any())).thenReturn(new TreeMap<>(){{
      put(0.0, edge1); put(160.0, edge2); put(240.0, edge3);
    }});
    Mockito.when(indexExtractor.split(Mockito.any(), Mockito.anyInt())).thenCallRealMethod();

    // then
    List<List<Edge<JunctionData, WayData>>> resultGroups = extractor.extract(List.of(edge1, edge2, edge3));
    Assertions.assertEquals(2, resultGroups.size());
    Assertions.assertTrue(resultGroups.stream().anyMatch(group -> group.size() == 2));
    Assertions.assertTrue(resultGroups.stream().anyMatch(group -> group.size() == 1));
    Assertions.assertTrue(resultGroups.stream()
        .filter(group -> group.size() == 1)
        .allMatch(group -> group.contains(edge3)));
  }

  @Test
  public void extractingThreeEdgesWithSplitPointFurther() {
    // given
    TwoByAngleGGRExtractor angleExtractor = Mockito.mock(TwoByAngleGGRExtractor.class);
    TwoByIndexGGRExtractor indexExtractor = Mockito.mock(TwoByIndexGGRExtractor.class);
    MixedTwoAngleIndexGGRExtractor extractor = new MixedTwoAngleIndexGGRExtractor(indexExtractor, angleExtractor);
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());

    // when
    Mockito.when(angleExtractor.create(Mockito.any(), Mockito.any())).thenReturn(new TreeMap<>(){{
      put(0.0, edge1); put(120.0, edge2); put(200.0, edge3);
    }});
    Mockito.when(indexExtractor.split(Mockito.any(), Mockito.anyInt())).thenCallRealMethod();

    // then
    List<List<Edge<JunctionData, WayData>>> resultGroups = extractor.extract(List.of(edge1, edge2, edge3));
    Assertions.assertEquals(2, resultGroups.size());
    Assertions.assertTrue(resultGroups.stream().anyMatch(group -> group.size() == 2));
    Assertions.assertTrue(resultGroups.stream().anyMatch(group -> group.size() == 1));
    Assertions.assertTrue(resultGroups.stream()
        .filter(group -> group.size() == 1)
        .allMatch(group -> group.contains(edge2)));
  }

  @Test
  public void extractingMoreEdgesWithSplitPointEqual() {
    // given
    TwoByAngleGGRExtractor angleExtractor = Mockito.mock(TwoByAngleGGRExtractor.class);
    TwoByIndexGGRExtractor indexExtractor = Mockito.mock(TwoByIndexGGRExtractor.class);
    MixedTwoAngleIndexGGRExtractor extractor = new MixedTwoAngleIndexGGRExtractor(indexExtractor, angleExtractor);
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().build());
    Edge<JunctionData, WayData> edge6 = new Edge<>("6", WayData.builder().build());

    // when
    Mockito.when(angleExtractor.create(Mockito.any(), Mockito.any())).thenReturn(new TreeMap<>(){{
      put(0.0, edge1); put(60.0, edge2); put(120.0, edge3); put(180.0, edge4); put(240.0, edge5); put(300.0, edge6);
    }});
    Mockito.when(indexExtractor.split(Mockito.any(), Mockito.anyInt())).thenCallRealMethod();

    // then
    List<List<Edge<JunctionData, WayData>>> resultGroups = extractor.extract(
        List.of(edge1, edge2, edge3, edge4, edge5, edge6));
    Assertions.assertEquals(3, resultGroups.size());
    Assertions.assertTrue(resultGroups.stream().allMatch(group -> group.size() == 2));
    Assertions.assertTrue(resultGroups.stream()
        .filter(group -> group.contains(edge1))
        .allMatch(group -> group.contains(edge4)));
    Assertions.assertTrue(resultGroups.stream()
        .filter(group -> group.contains(edge2))
        .allMatch(group -> group.contains(edge5)));
    Assertions.assertTrue(resultGroups.stream()
        .filter(group -> group.contains(edge3))
        .allMatch(group -> group.contains(edge6)));
  }

  @Test
  public void extractingMoreEdgesWithSplitPointNonEqual() {
    // given
    TwoByAngleGGRExtractor angleExtractor = Mockito.mock(TwoByAngleGGRExtractor.class);
    TwoByIndexGGRExtractor indexExtractor = Mockito.mock(TwoByIndexGGRExtractor.class);
    MixedTwoAngleIndexGGRExtractor extractor = new MixedTwoAngleIndexGGRExtractor(indexExtractor, angleExtractor);
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().build());
    Edge<JunctionData, WayData> edge6 = new Edge<>("6", WayData.builder().build());

    // when
    Mockito.when(angleExtractor.create(Mockito.any(), Mockito.any())).thenReturn(new TreeMap<>(){{
      put(0.0, edge1); put(60.0, edge2); put(180.0, edge3); put(200.0, edge4); put(240.0, edge5); put(300.0, edge6);
    }});
    Mockito.when(indexExtractor.split(Mockito.any(), Mockito.anyInt())).thenCallRealMethod();

    // then
    List<List<Edge<JunctionData, WayData>>> resultGroups = extractor.extract(
        List.of(edge1, edge2, edge3, edge4, edge5, edge6));
    Assertions.assertEquals(4, resultGroups.size());
    Assertions.assertEquals(2, resultGroups.stream()
        .filter(group -> group.size() == 2)
        .count());
    Assertions.assertEquals(2, resultGroups.stream()
        .filter(group -> group.size() == 1)
        .count());
    Assertions.assertTrue(resultGroups.stream()
        .filter(group -> group.contains(edge1))
        .allMatch(group -> group.contains(edge3)));
    Assertions.assertTrue(resultGroups.stream()
        .filter(group -> group.contains(edge2))
        .allMatch(group -> group.contains(edge4)));
    Assertions.assertTrue(resultGroups.stream()
        .filter(group -> group.contains(edge5))
        .allMatch(group -> group.size() == 1));
    Assertions.assertTrue(resultGroups.stream()
        .filter(group -> group.contains(edge6))
        .allMatch(group -> group.size() == 1));
  }
}
