package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@ExtendWith(MockitoExtension.class)
public class TwoByAngleGGRExtractorTest {

  @Test
  public void creatingMapOfAngleToEdge() {
    // given
    TwoByAngleGGRExtractor extractor = new TwoByAngleGGRExtractor();
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).lat(50.0850637).tags(new HashMap<>()).lon(19.8911986).build());
    Node<JunctionData, WayData> firstNode = new Node<>("1",
        JunctionData.builder().isCrossroad(false).lat(50.0843471).tags(new HashMap<>()).lon(19.8911159).build());
    Node<JunctionData, WayData> secondNode = new Node<>("2",
        JunctionData.builder().isCrossroad(false).lat(50.0852816).tags(new HashMap<>()).lon(19.8912199).build());
    Node<JunctionData, WayData> thirdNode = new Node<>("3",
        JunctionData.builder().isCrossroad(false).lat(50.0852747).tags(new HashMap<>()).lon(19.8913083).build());

    Edge<JunctionData, WayData> inEdge1 = new Edge<>("10", WayData.builder().build());
    Edge<JunctionData, WayData> inEdge2 = new Edge<>("20", WayData.builder().build());
    Edge<JunctionData, WayData> inEdge3 = new Edge<>("30", WayData.builder().build());

    // when
    inEdge1.setSource(firstNode);
    inEdge1.setTarget(crossroad);
    inEdge2.setSource(secondNode);
    inEdge2.setTarget(crossroad);
    inEdge3.setSource(thirdNode);
    inEdge3.setTarget(crossroad);
    TreeMap<Double, Edge<JunctionData, WayData>> resultMap = extractor.create(List.of(inEdge1, inEdge2, inEdge3), inEdge1);

    // then
    Assertions.assertEquals(inEdge1, resultMap.ceilingEntry(0.0).getValue());
    Assertions.assertEquals(inEdge2, resultMap.ceilingEntry(160.0).getValue());
    Assertions.assertEquals(inEdge3, resultMap.ceilingEntry(190.0).getValue());
    List<Edge<JunctionData, WayData>> orderResultList = new ArrayList<>(resultMap.values());
    Assertions.assertEquals(inEdge1, orderResultList.get(0));
    Assertions.assertEquals(inEdge2, orderResultList.get(1));
    Assertions.assertEquals(inEdge3, orderResultList.get(2));
  }

  @Test
  public void extractingZeroEdges() {
    // given
    TwoByAngleGGRExtractor extractor = new TwoByAngleGGRExtractor();

    // when
    List<List<Edge<JunctionData, WayData>>> resultGroups = extractor.extract(List.of());

    // then
    Assertions.assertEquals(0, resultGroups.size());
  }

  @Test
  public void extractingTwoEdges() {
    // given
    TwoByAngleGGRExtractor extractor = new TwoByAngleGGRExtractor();
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
  public void extractingThreeEdgesAsThreeGroups() {
    // given
    TwoByAngleGGRExtractor extractor = Mockito.mock(TwoByAngleGGRExtractor.class);
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());

    // when
    Mockito.when(extractor.create(Mockito.any(), Mockito.any())).thenReturn(new TreeMap<>(){{
      put(0.0, edge1); put(120.0, edge2); put(240.0, edge3);
    }});
    Mockito.when(extractor.extract(Mockito.any())).thenCallRealMethod();

    // then
    List<List<Edge<JunctionData, WayData>>> resultGroups = extractor.extract(List.of(edge1, edge2, edge3));
    Assertions.assertEquals(3, resultGroups.size());
    Assertions.assertEquals(1, resultGroups.get(0).size());
    Assertions.assertEquals(1, resultGroups.get(1).size());
    Assertions.assertEquals(1, resultGroups.get(2).size());
  }

  @Test
  public void extractingThreeEdgesAsTwoGroups() {
    // given
    TwoByAngleGGRExtractor extractor = Mockito.mock(TwoByAngleGGRExtractor.class);
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());

    // when
    Mockito.when(extractor.create(Mockito.any(), Mockito.any())).thenReturn(new TreeMap<>(){{
      put(0.0, edge1); put(170.0, edge2); put(240.0, edge3);
    }});
    Mockito.when(extractor.extract(Mockito.any())).thenCallRealMethod();

    // then
    List<List<Edge<JunctionData, WayData>>> resultGroups = extractor.extract(List.of(edge1, edge2, edge3));
    Assertions.assertEquals(2, resultGroups.size());
    Assertions.assertTrue(resultGroups.stream().anyMatch(group -> group.size() == 2));
    Assertions.assertTrue(resultGroups.stream().anyMatch(group -> group.size() == 1));
  }

  @Test
  public void extractingThreeEdgesAsTwoGroupsButWithTwoCandidates() {
    // given
    TwoByAngleGGRExtractor extractor = Mockito.mock(TwoByAngleGGRExtractor.class);
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());

    // when
    Mockito.when(extractor.create(Mockito.any(), Mockito.any())).thenReturn(new TreeMap<>(){{
      put(0.0, edge1); put(170.0, edge2); put(195.0, edge3);
    }});
    Mockito.when(extractor.extract(Mockito.any())).thenCallRealMethod();

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
  public void extractingMoreEdgesAsMoreEqualGroups() {
    // given
    TwoByAngleGGRExtractor extractor = Mockito.mock(TwoByAngleGGRExtractor.class);
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().build());
    Edge<JunctionData, WayData> edge6 = new Edge<>("6", WayData.builder().build());

    // when
    Mockito.when(extractor.create(Mockito.any(), Mockito.any())).thenReturn(new TreeMap<>(){{
      put(0.0, edge1); put(40.0, edge2); put(100.0, edge3); put(170.0, edge4); put(195.0, edge5); put(260.0, edge6);
    }});
    Mockito.when(extractor.extract(Mockito.any())).thenCallRealMethod();

    // then
    List<List<Edge<JunctionData, WayData>>> resultGroups = extractor.extract(
        List.of(edge1, edge2, edge3, edge4, edge5, edge6));
    Assertions.assertEquals(3, resultGroups.size());
    Assertions.assertTrue(resultGroups.stream().allMatch(group -> group.size() == 2));
  }

  @Test
  public void extractingMoreEdgesAsMoreNonEqualGroups() {
    // given
    TwoByAngleGGRExtractor extractor = Mockito.mock(TwoByAngleGGRExtractor.class);
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().build());
    Edge<JunctionData, WayData> edge6 = new Edge<>("6", WayData.builder().build());

    // when
    Mockito.when(extractor.create(Mockito.any(), Mockito.any())).thenReturn(new TreeMap<>(){{
      put(0.0, edge1); put(40.0, edge2); put(100.0, edge3); put(170.0, edge4); put(195.0, edge5); put(220.0, edge6);
    }});
    Mockito.when(extractor.extract(Mockito.any())).thenCallRealMethod();

    // then
    List<List<Edge<JunctionData, WayData>>> resultGroups = extractor.extract(
        List.of(edge1, edge2, edge3, edge4, edge5, edge6));
    Assertions.assertEquals(4, resultGroups.size());
    Assertions.assertEquals(2, resultGroups.stream().filter(group -> group.size() == 2).count());
    Assertions.assertEquals(2, resultGroups.stream().filter(group -> group.size() == 1).count());
  }
}
