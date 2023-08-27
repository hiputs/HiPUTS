package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.OnCrossroadSuccessorAllocator;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.relation.Restriction;
import pl.edu.agh.hiputs.partition.model.relation.RestrictionType;

@ExtendWith(MockitoExtension.class)
public class RestrictionsCorrectorTest {
  private final OnCrossroadSuccessorAllocator allocator =
      Mockito.mock(OnCrossroadSuccessorAllocator.class);
  @Captor
  private ArgumentCaptor<Set<Restriction>> contextCaptor;

  @Test
  public void emptyGraphEmptySet() {
    // given

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().build();
    RestrictionsCorrector corrector = new RestrictionsCorrector(Collections.emptySet(), allocator);

    // then
    corrector.correct(graph);
    Mockito.verify(allocator, Mockito.times(1)).provideRestrictions(Mockito.any());
    Mockito.verify(allocator).provideRestrictions(contextCaptor.capture());
    Set<Restriction> providedRestrictions = contextCaptor.getValue();
    Assertions.assertTrue(providedRestrictions.isEmpty());
  }

  @Test
  public void emptyGraphOnly() {
    // given

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().build();
    RestrictionsCorrector corrector = new RestrictionsCorrector(new HashSet<>(Set.of(
        Restriction.builder()
            .id("id")
            .type(RestrictionType.NO_ENTRY)
            .fromEdgeId("1")
            .viaNodeId("B")
            .toEdgeId("2")
            .build()
    )), allocator);

    // then
    corrector.correct(graph);
    Mockito.verify(allocator, Mockito.times(1)).provideRestrictions(Mockito.any());
    Mockito.verify(allocator).provideRestrictions(contextCaptor.capture());
    Set<Restriction> providedRestrictions = contextCaptor.getValue();
    Assertions.assertTrue(providedRestrictions.isEmpty());
  }

  @Test
  public void emptySetOnly() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();
    RestrictionsCorrector corrector = new RestrictionsCorrector(Collections.emptySet(), allocator);

    // then
    corrector.correct(graph);
    Mockito.verify(allocator, Mockito.times(1)).provideRestrictions(Mockito.any());
    Mockito.verify(allocator).provideRestrictions(contextCaptor.capture());
    Set<Restriction> providedRestrictions = contextCaptor.getValue();
    Assertions.assertTrue(providedRestrictions.isEmpty());
  }

  @Test
  public void allUpToDate() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();
    RestrictionsCorrector corrector = new RestrictionsCorrector(new HashSet<>(Set.of(
        Restriction.builder()
            .id("id")
            .type(RestrictionType.NO_ENTRY)
            .fromEdgeId("1")
            .viaNodeId("B")
            .toEdgeId("2")
            .build()
    )), allocator);

    // then
    corrector.correct(graph);
    Mockito.verify(allocator, Mockito.times(1)).provideRestrictions(Mockito.any());
    Mockito.verify(allocator).provideRestrictions(contextCaptor.capture());
    Set<Restriction> providedRestrictions = contextCaptor.getValue();
    Assertions.assertEquals(1, providedRestrictions.size());
  }

  @Test
  public void lackOfFromEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge2)
        .build();
    RestrictionsCorrector corrector = new RestrictionsCorrector(new HashSet<>(Set.of(
        Restriction.builder()
            .id("id")
            .type(RestrictionType.NO_ENTRY)
            .fromEdgeId("1")
            .viaNodeId("B")
            .toEdgeId("2")
            .build()
    )), allocator);

    // then
    corrector.correct(graph);
    Mockito.verify(allocator, Mockito.times(1)).provideRestrictions(Mockito.any());
    Mockito.verify(allocator).provideRestrictions(contextCaptor.capture());
    Set<Restriction> providedRestrictions = contextCaptor.getValue();
    Assertions.assertTrue(providedRestrictions.isEmpty());
  }

  @Test
  public void lackOfToEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .build();
    RestrictionsCorrector corrector = new RestrictionsCorrector(new HashSet<>(Set.of(
        Restriction.builder()
            .id("id")
            .type(RestrictionType.NO_ENTRY)
            .fromEdgeId("1")
            .viaNodeId("B")
            .toEdgeId("2")
            .build()
    )), allocator);

    // then
    corrector.correct(graph);
    Mockito.verify(allocator, Mockito.times(1)).provideRestrictions(Mockito.any());
    Mockito.verify(allocator).provideRestrictions(contextCaptor.capture());
    Set<Restriction> providedRestrictions = contextCaptor.getValue();
    Assertions.assertTrue(providedRestrictions.isEmpty());
  }

  @Test
  public void lackOfViaNode() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();
    RestrictionsCorrector corrector = new RestrictionsCorrector(new HashSet<>(Set.of(
        Restriction.builder()
            .id("id")
            .type(RestrictionType.NO_ENTRY)
            .fromEdgeId("1")
            .viaNodeId("X")
            .toEdgeId("2")
            .build()
    )), allocator);

    // then
    corrector.correct(graph);
    Mockito.verify(allocator, Mockito.times(1)).provideRestrictions(Mockito.any());
    Mockito.verify(allocator).provideRestrictions(contextCaptor.capture());
    Set<Restriction> providedRestrictions = contextCaptor.getValue();
    Assertions.assertTrue(providedRestrictions.isEmpty());
  }
}
