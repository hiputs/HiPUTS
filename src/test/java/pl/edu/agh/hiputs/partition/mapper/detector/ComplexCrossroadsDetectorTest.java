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
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.StandardDetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor.StandardDetectorStrategyExecutor;
import pl.edu.agh.hiputs.partition.mapper.detector.util.complex.ComplexCrossroadsFinder;
import pl.edu.agh.hiputs.partition.mapper.detector.util.complex.ComplexRoundaboutFinder;
import pl.edu.agh.hiputs.partition.mapper.detector.util.complex.ComplexityFinder;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroadsRepositoryImpl;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@ExtendWith(MockitoExtension.class)
public class ComplexCrossroadsDetectorTest {

  private final ModelConfigurationService modelConfigurationService =
      Mockito.mock(ModelConfigurationService.class, Mockito.RETURNS_DEEP_STUBS);
  private final StandardDetectorStrategyExecutor standardDetectorStrategyExecutor =
      Mockito.mock(StandardDetectorStrategyExecutor.class);
  private final List<ComplexityFinder> finders = new ArrayList<>();
  private final ComplexCrossroadsRepositoryImpl wrapper = Mockito.mock(ComplexCrossroadsRepositoryImpl.class);
  private final ComplexCrossroadsDetector detector =
      new ComplexCrossroadsDetector(standardDetectorStrategyExecutor, finders, wrapper);
  @Captor
  private ArgumentCaptor<StandardDetectorContext> contextCaptor;

  @BeforeEach
  public void init() {
    finders.clear();
    finders.addAll(List.of(new ComplexCrossroadsFinder(modelConfigurationService), new ComplexRoundaboutFinder()));
  }

  @Test
  public void happyPath() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(1).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).length(1).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge6 = new Edge<>("6", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge7 = new Edge<>("7", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge8 = new Edge<>("8", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge9 = new Edge<>("9", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge10 = new Edge<>("10", WayData.builder().tags(new HashMap<>()).length(4).build());
    Node<JunctionData, WayData> nodeA =
        new Node<>("A", JunctionData.builder().isCrossroad(true).lat(50.0945494).lon(19.8775622).build());
    Node<JunctionData, WayData> nodeB =
        new Node<>("B", JunctionData.builder().isCrossroad(true).lat(50.0942335).lon(19.8788777).build());
    Node<JunctionData, WayData> nodeC =
        new Node<>("C", JunctionData.builder().isCrossroad(false).lat(50.0932701).lon(19.8772253).build());
    Node<JunctionData, WayData> nodeD =
        new Node<>("D", JunctionData.builder().isCrossroad(false).lat(50.0956796).lon(19.8784069).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeA);
    edge3.setSource(nodeA);
    edge3.setTarget(nodeC);
    edge4.setSource(nodeC);
    edge4.setTarget(nodeA);
    edge5.setSource(nodeB);
    edge5.setTarget(nodeC);
    edge6.setSource(nodeC);
    edge6.setTarget(nodeB);
    edge7.setSource(nodeB);
    edge7.setTarget(nodeD);
    edge8.setSource(nodeD);
    edge8.setTarget(nodeB);
    edge9.setSource(nodeA);
    edge9.setTarget(nodeD);
    edge10.setSource(nodeD);
    edge10.setTarget(nodeA);
    nodeA.getIncomingEdges().addAll(List.of(edge2, edge4, edge10));
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge3, edge9));
    nodeB.getIncomingEdges().addAll(List.of(edge1, edge6, edge8));
    nodeB.getOutgoingEdges().addAll(List.of(edge2, edge7, edge5));
    nodeC.getIncomingEdges().addAll(List.of(edge3, edge5));
    nodeC.getOutgoingEdges().addAll(List.of(edge4, edge6));
    nodeD.getIncomingEdges().addAll(List.of(edge7, edge9));
    nodeD.getOutgoingEdges().addAll(List.of(edge8, edge10));
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .addEdge(edge4)
        .addEdge(edge5)
        .addEdge(edge6)
        .addEdge(edge7)
        .addEdge(edge8)
        .addEdge(edge9)
        .addEdge(edge10)
        .build();
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(2.0);

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
