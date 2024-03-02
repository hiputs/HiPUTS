package pl.edu.agh.hiputs.partition.mapper.detector;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.OnCrossroadSuccessorAllocator;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.StandardDetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor.StandardDetectorStrategyExecutor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.relation.Restriction;

@ExtendWith(MockitoExtension.class)
public class RestrictionsDetectorTest {

  private final StandardDetectorStrategyExecutor standardDetectorStrategyExecutor =
      Mockito.mock(StandardDetectorStrategyExecutor.class);
  private final OnCrossroadSuccessorAllocator allocator = Mockito.mock(OnCrossroadSuccessorAllocator.class);
  private final RestrictionsDetector detector = new RestrictionsDetector(standardDetectorStrategyExecutor, allocator);
  @Captor
  private ArgumentCaptor<StandardDetectorContext> contextCaptor;

  @Test
  public void happyPath() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().build());
    Node<JunctionData, WayData> nodeA =
        new Node<>("A", JunctionData.builder().restrictions(List.of(Restriction.builder().build())).build());
    Node<JunctionData, WayData> nodeB =
        new Node<>("B", JunctionData.builder().restrictions(List.of(Restriction.builder().build())).build());
    Node<JunctionData, WayData> nodeC =
        new Node<>("C", JunctionData.builder().restrictions(List.of(Restriction.builder().build())).build());
    Node<JunctionData, WayData> nodeD =
        new Node<>("D", JunctionData.builder().restrictions(List.of(Restriction.builder().build())).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeD);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    nodeD.getIncomingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    detector.detect(graph);
    Mockito.verify(standardDetectorStrategyExecutor, Mockito.times(1))
        .followAppropriateStrategy(Mockito.any(), Mockito.any());
    Mockito.verify(standardDetectorStrategyExecutor).followAppropriateStrategy(Mockito.any(), contextCaptor.capture());
    StandardDetectorContext context = contextCaptor.getValue();
    Assertions.assertTrue(context.getDetectionReport().isPresent());
    Assertions.assertTrue(context.getPreparedCorrector().isPresent());
  }
}
