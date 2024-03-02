package pl.edu.agh.hiputs.partition.mapper.detector;

import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.factory.ConnectivityCorrectorStrategyFactory;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.DirectBridgesConnectFixer;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.StandardDetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor.StandardDetectorStrategyExecutor;
import pl.edu.agh.hiputs.partition.mapper.detector.util.connectivity.CCFinder;
import pl.edu.agh.hiputs.partition.mapper.detector.util.connectivity.KosarajuSCCFinder;
import pl.edu.agh.hiputs.partition.mapper.detector.util.connectivity.TypicalWCCFinder;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.reflector.StandardEdgeReflector;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@ExtendWith(MockitoExtension.class)
public class MapDisConnectivityDetectorTest {

  private final ConnectivityCorrectorStrategyFactory connectivityCorrectorStrategyFactory =
      Mockito.mock(ConnectivityCorrectorStrategyFactory.class);
  private final StandardDetectorStrategyExecutor standardDetectorStrategyExecutor =
      Mockito.mock(StandardDetectorStrategyExecutor.class);
  private final CCFinder<StronglyConnectedComponent> sCCFinder = new KosarajuSCCFinder();
  private final CCFinder<WeaklyConnectedComponent> wCCFinder = new TypicalWCCFinder();
  private final MapDisConnectivityDetector mapDisConnectivityDetector =
      new MapDisConnectivityDetector(connectivityCorrectorStrategyFactory, standardDetectorStrategyExecutor, sCCFinder,
          wCCFinder);
  @Captor
  private ArgumentCaptor<StandardDetectorContext> contextCaptor;

  @Test
  public void happyPath() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeD);
    edge3.setSource(nodeD);
    edge3.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge3);
    nodeD.getOutgoingEdges().add(edge3);
    nodeD.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    Mockito.when(connectivityCorrectorStrategyFactory.getFromConfiguration())
        .thenReturn(new DirectBridgesConnectFixer(new StandardEdgeReflector()));

    // then
    mapDisConnectivityDetector.detect(graph);
    Mockito.verify(standardDetectorStrategyExecutor, Mockito.times(1))
        .followAppropriateStrategy(Mockito.any(), Mockito.any());
    Mockito.verify(standardDetectorStrategyExecutor).followAppropriateStrategy(Mockito.any(), contextCaptor.capture());
    StandardDetectorContext context = contextCaptor.getValue();
    Assertions.assertTrue(context.getDetectionReport().isPresent());
    Assertions.assertTrue(context.getPreparedCorrector().isPresent());
  }
}
