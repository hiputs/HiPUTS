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
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.factory.WrongConnectionsCorrectorStrategyFactory;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.compatibility.ChangeTypesFixer;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.StandardDetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor.StandardDetectorStrategyExecutor;
import pl.edu.agh.hiputs.partition.mapper.detector.util.compatibility.IncompatibilityFinder;
import pl.edu.agh.hiputs.partition.mapper.detector.util.compatibility.OnMotorwaysIncompatibilityFinder;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@ExtendWith(MockitoExtension.class)
public class WrongConnectionsDetectorTest {

  private final WrongConnectionsCorrectorStrategyFactory wrongConnectionsCorrectorStrategyFactory =
      Mockito.mock(WrongConnectionsCorrectorStrategyFactory.class);
  private final StandardDetectorStrategyExecutor standardDetectorStrategyExecutor =
      Mockito.mock(StandardDetectorStrategyExecutor.class);
  private final List<IncompatibilityFinder> finders = new ArrayList<>();
  private final WrongConnectionsDetector detector =
      new WrongConnectionsDetector(wrongConnectionsCorrectorStrategyFactory, standardDetectorStrategyExecutor, finders);
  @Captor
  private ArgumentCaptor<StandardDetectorContext> contextCaptor;

  @BeforeEach
  public void init() {
    finders.clear();
    finders.add(new OnMotorwaysIncompatibilityFinder());
  }

  @Test
  public void happyPath() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());

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
    Mockito.when(wrongConnectionsCorrectorStrategyFactory.getFromConfiguration()).thenReturn(new ChangeTypesFixer());

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
