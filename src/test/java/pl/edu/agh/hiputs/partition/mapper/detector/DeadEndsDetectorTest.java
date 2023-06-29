package pl.edu.agh.hiputs.partition.mapper.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.factory.DeadEndsCorrectorStrategyFactory;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end.AddReversesOnDeadEndsFixer;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.StandardDetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor.StandardDetectorStrategyExecutor;
import pl.edu.agh.hiputs.partition.mapper.detector.util.end.ByIncomingDeadEndsFinder;
import pl.edu.agh.hiputs.partition.mapper.detector.util.end.ByOutgoingDeadEndsFinder;
import pl.edu.agh.hiputs.partition.mapper.detector.util.end.DeadEndsFinder;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@ExtendWith(MockitoExtension.class)
public class DeadEndsDetectorTest {
  private final DeadEndsCorrectorStrategyFactory deadEndsCorrectorStrategyFactory =
      Mockito.mock(DeadEndsCorrectorStrategyFactory.class);
  private final StandardDetectorStrategyExecutor standardDetectorStrategyExecutor =
      Mockito.mock(StandardDetectorStrategyExecutor.class);
  private final List<DeadEndsFinder> finders = new ArrayList<>();
  private final DeadEndsDetector detector = new DeadEndsDetector(
      deadEndsCorrectorStrategyFactory, standardDetectorStrategyExecutor, finders);
  @Captor
  private ArgumentCaptor<StandardDetectorContext> contextCaptor;

  @BeforeEach
  public void init() {
    finders.clear();
    finders.addAll(List.of(new ByIncomingDeadEndsFinder(), new ByOutgoingDeadEndsFinder()));
  }

  @Test
  public void happyPath() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeE = new Node<>("D", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeD);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeA);
    edge4.setSource(nodeA);
    edge4.setTarget(nodeE);
    edge5.setSource(nodeE);
    edge5.setTarget(nodeA);
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge4));
    nodeA.getIncomingEdges().addAll(List.of(edge5, edge3));
    nodeB.getIncomingEdges().add(edge1);
    nodeD.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    nodeE.getIncomingEdges().add(edge4);
    nodeE.getOutgoingEdges().add(edge5);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addNode(nodeE)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .addEdge(edge4)
        .addEdge(edge5)
        .build();
    Mockito.when(deadEndsCorrectorStrategyFactory.getFromConfiguration()).thenReturn(new AddReversesOnDeadEndsFixer());

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
