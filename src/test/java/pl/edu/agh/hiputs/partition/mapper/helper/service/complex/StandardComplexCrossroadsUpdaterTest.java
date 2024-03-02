package pl.edu.agh.hiputs.partition.mapper.helper.service.complex;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.helper.service.crossroad.StandardCrossroadDeterminer;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroad;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroadsRepository;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroadsRepositoryImpl;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@ExtendWith(MockitoExtension.class)
public class StandardComplexCrossroadsUpdaterTest {

  private final ModelConfigurationService modelConfigService =
      Mockito.mock(ModelConfigurationService.class, Mockito.RETURNS_DEEP_STUBS);
  private final StandardCrossroadDeterminer determiner = Mockito.mock(StandardCrossroadDeterminer.class);
  private final ComplexCrossroadsRepository repository = new ComplexCrossroadsRepositoryImpl();
  private final StandardComplexCrossroadsUpdater updater =
      new StandardComplexCrossroadsUpdater(determiner, repository, modelConfigService);

  @BeforeEach
  public void init() {
    repository.getComplexCrossroads().clear();
  }

  @Test
  public void emptyAll() {
    // given

    // when

    // then
    updater.extendWithNodes(Set.of());
    Assertions.assertTrue(repository.getComplexCrossroads().isEmpty());
  }

  @Test
  public void emptyRepoAndOneCrossroadNode() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());

    // when
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);

    // then
    updater.extendWithNodes(Set.of(nodeA));
    Assertions.assertTrue(repository.getComplexCrossroads().isEmpty());
  }

  @Test
  public void emptyRepoAndOneCrossroadNodeWithFarNeighbour() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(15).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);

    // then
    updater.extendWithNodes(Set.of(nodeA));
    Assertions.assertTrue(repository.getComplexCrossroads().isEmpty());
  }

  @Test
  public void emptyRepoAndOneCrossroadNodeWithCloseNoCrossroadNeighbour() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(false);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);

    // then
    updater.extendWithNodes(Set.of(nodeA));
    Assertions.assertTrue(repository.getComplexCrossroads().isEmpty());
  }

  @Test
  public void emptyRepoAndOneNoCrossroadNodeWithCloseCrossroadNeighbour() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Mockito.when(determiner.determine(nodeA)).thenReturn(false);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);

    // then
    updater.extendWithNodes(Set.of(nodeA));
    Assertions.assertTrue(repository.getComplexCrossroads().isEmpty());
  }

  @Test
  public void emptyRepoAndOneCrossroadNodeWithCloseCrossroadNeighbour() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);

    // then
    updater.extendWithNodes(Set.of(nodeA));
    Assertions.assertEquals(1, repository.getComplexCrossroads().size());
  }

  @Test
  public void emptyRepoAndTwoSeparateCrossroadNodesWithCloseCrossroadNeighbours() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(7).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeD);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    nodeD.getIncomingEdges().add(edge2);
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(true);
    Mockito.when(determiner.determine(nodeD)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC));
    Assertions.assertEquals(2, repository.getComplexCrossroads().size());
  }

  @Test
  public void emptyRepoAndTwoSeparateCrossroadNodesConnectedByCloseCrossroadNeighbour() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(7).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().addAll(List.of(edge1, edge2));
    nodeC.getOutgoingEdges().add(edge2);
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC));
    Assertions.assertEquals(1, repository.getComplexCrossroads().size());
  }

  @Test
  public void emptyRepoAndTwoConnectedCrossroadNodesWithCloseCrossroadNeighbours() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(7).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().length(6).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeD);
    edge3.setSource(nodeA);
    edge3.setTarget(nodeC);
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge3));
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge3);
    nodeD.getIncomingEdges().add(edge2);
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(true);
    Mockito.when(determiner.determine(nodeD)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC));
    Assertions.assertEquals(1, repository.getComplexCrossroads().size());
  }

  @Test
  public void oneCCRepoAndOneFarCrossroadNode() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(11).build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeA);
    nodeA.getOutgoingEdges().add(edge1);
    nodeA.getIncomingEdges().add(edge2);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);
    repository.getComplexCrossroads().add(complexCrossroad1);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC));
    Assertions.assertEquals(1, repository.getComplexCrossroads().size());
    Assertions.assertTrue(
        repository.getComplexCrossroads().stream().noneMatch(cc -> cc.getNodesIdsIn().contains(nodeC.getId())));
  }

  @Test
  public void oneCCRepoAndOneCloseCrossroadNode() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(9).build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeA);
    nodeA.getOutgoingEdges().add(edge1);
    nodeA.getIncomingEdges().add(edge2);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);
    repository.getComplexCrossroads().add(complexCrossroad1);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC));
    Assertions.assertEquals(1, repository.getComplexCrossroads().size());
    Assertions.assertTrue(
        repository.getComplexCrossroads().stream().allMatch(cc -> cc.getNodesIdsIn().contains(nodeC.getId())));
  }

  @Test
  public void oneCCRepoAndOneCloseNoCrossroadNode() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(9).build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeA);
    nodeA.getOutgoingEdges().add(edge1);
    nodeA.getIncomingEdges().add(edge2);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(false);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);
    repository.getComplexCrossroads().add(complexCrossroad1);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC));
    Assertions.assertEquals(1, repository.getComplexCrossroads().size());
    Assertions.assertTrue(
        repository.getComplexCrossroads().stream().noneMatch(cc -> cc.getNodesIdsIn().contains(nodeC.getId())));
  }

  @Test
  public void oneCCRepoAndTwoNotConnectedCloseCrossroadNodes() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(9).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().length(7).build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeA);
    edge3.setSource(nodeB);
    edge3.setTarget(nodeD);
    nodeA.getOutgoingEdges().add(edge1);
    nodeA.getIncomingEdges().add(edge2);
    nodeB.getOutgoingEdges().add(edge3);
    nodeB.getOutgoingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    nodeD.getIncomingEdges().add(edge3);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(true);
    Mockito.when(determiner.determine(nodeD)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);
    repository.getComplexCrossroads().add(complexCrossroad1);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC, nodeB, nodeD));
    Assertions.assertEquals(1, repository.getComplexCrossroads().size());
    Assertions.assertTrue(repository.getComplexCrossroads()
        .stream()
        .allMatch(cc -> cc.getNodesIdsIn().contains(nodeC.getId()) && cc.getNodesIdsIn().contains(nodeD.getId())));
  }

  @Test
  public void oneCCRepoAndTwoConnectedCloseCrossroadNodes() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(9).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().length(7).build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeA);
    edge3.setSource(nodeD);
    edge3.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeA.getIncomingEdges().add(edge2);
    nodeB.getOutgoingEdges().add(edge1);
    nodeC.getIncomingEdges().add(edge3);
    nodeC.getOutgoingEdges().add(edge2);
    nodeD.getIncomingEdges().add(edge3);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(true);
    Mockito.when(determiner.determine(nodeD)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);
    repository.getComplexCrossroads().add(complexCrossroad1);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC));
    Assertions.assertEquals(1, repository.getComplexCrossroads().size());
    Assertions.assertTrue(repository.getComplexCrossroads()
        .stream()
        .allMatch(cc -> cc.getNodesIdsIn().contains(nodeC.getId()) && cc.getNodesIdsIn().contains(nodeD.getId())));
  }

  @Test
  public void twoCCsRepoAndOneConnectedCrossroadNodeCloseToOneCC() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeE = new Node<>("E", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(9).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().length(7).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().length(11).build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();
    ComplexCrossroad complexCrossroad2 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeD);
    edge3.setSource(nodeE);
    edge3.setTarget(nodeA);
    edge4.setSource(nodeE);
    edge4.setTarget(nodeC);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getIncomingEdges().add(edge4);
    nodeC.getOutgoingEdges().add(edge2);
    nodeD.getIncomingEdges().add(edge2);
    nodeE.getOutgoingEdges().addAll(List.of(edge3, edge4));
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    complexCrossroad2.addNode(nodeC.getId());
    complexCrossroad2.addNode(nodeD.getId());
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(true);
    Mockito.when(determiner.determine(nodeD)).thenReturn(true);
    Mockito.when(determiner.determine(nodeE)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);
    repository.getComplexCrossroads().add(complexCrossroad1);
    repository.getComplexCrossroads().add(complexCrossroad2);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC, nodeE));
    Assertions.assertEquals(2, repository.getComplexCrossroads().size());
  }

  @Test
  public void twoCCsRepoAndOneConnectedCrossroadNodeCloseToBothCC() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeE = new Node<>("E", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(9).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().length(7).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().length(4).build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();
    ComplexCrossroad complexCrossroad2 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeD);
    edge3.setSource(nodeE);
    edge3.setTarget(nodeA);
    edge4.setSource(nodeE);
    edge4.setTarget(nodeC);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getIncomingEdges().add(edge4);
    nodeC.getOutgoingEdges().add(edge2);
    nodeD.getIncomingEdges().add(edge2);
    nodeE.getOutgoingEdges().addAll(List.of(edge3, edge4));
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    complexCrossroad2.addNode(nodeC.getId());
    complexCrossroad2.addNode(nodeD.getId());
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(true);
    Mockito.when(determiner.determine(nodeD)).thenReturn(true);
    Mockito.when(determiner.determine(nodeE)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);
    repository.getComplexCrossroads().add(complexCrossroad1);
    repository.getComplexCrossroads().add(complexCrossroad2);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC, nodeE));
    Assertions.assertEquals(1, repository.getComplexCrossroads().size());
    Assertions.assertTrue(
        repository.getComplexCrossroads().stream().allMatch(cc -> cc.getNodesIdsIn().contains(nodeE.getId())));
  }

  @Test
  public void twoCCsRepoAndTwoConnectedCrossroadNodesCloseToBothCCButNotBetween() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeE = new Node<>("E", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeF = new Node<>("F", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(9).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().length(7).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().length(12).build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().length(4).build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();
    ComplexCrossroad complexCrossroad2 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeD);
    edge3.setSource(nodeE);
    edge3.setTarget(nodeA);
    edge4.setSource(nodeE);
    edge4.setTarget(nodeF);
    edge5.setSource(nodeF);
    edge5.setTarget(nodeC);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getIncomingEdges().add(edge5);
    nodeC.getOutgoingEdges().add(edge2);
    nodeD.getIncomingEdges().add(edge2);
    nodeE.getOutgoingEdges().addAll(List.of(edge3, edge4));
    nodeF.getIncomingEdges().add(edge4);
    nodeF.getOutgoingEdges().add(edge5);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    complexCrossroad2.addNode(nodeC.getId());
    complexCrossroad2.addNode(nodeD.getId());
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(true);
    Mockito.when(determiner.determine(nodeD)).thenReturn(true);
    Mockito.when(determiner.determine(nodeE)).thenReturn(true);
    Mockito.when(determiner.determine(nodeF)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);
    repository.getComplexCrossroads().add(complexCrossroad1);
    repository.getComplexCrossroads().add(complexCrossroad2);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC, nodeE, nodeF));
    Assertions.assertEquals(2, repository.getComplexCrossroads().size());
  }

  @Test
  public void twoCCsRepoAndTwoConnectedCrossroadNodesCloseToBothCC() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeE = new Node<>("E", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeF = new Node<>("F", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().length(5).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().length(9).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().length(7).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().length(6).build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().length(4).build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();
    ComplexCrossroad complexCrossroad2 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeD);
    edge3.setSource(nodeE);
    edge3.setTarget(nodeA);
    edge4.setSource(nodeE);
    edge4.setTarget(nodeF);
    edge5.setSource(nodeF);
    edge5.setTarget(nodeC);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getIncomingEdges().add(edge5);
    nodeC.getOutgoingEdges().add(edge2);
    nodeD.getIncomingEdges().add(edge2);
    nodeE.getOutgoingEdges().addAll(List.of(edge3, edge4));
    nodeF.getIncomingEdges().add(edge4);
    nodeF.getOutgoingEdges().add(edge5);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    complexCrossroad2.addNode(nodeC.getId());
    complexCrossroad2.addNode(nodeD.getId());
    Mockito.when(determiner.determine(nodeA)).thenReturn(true);
    Mockito.when(determiner.determine(nodeB)).thenReturn(true);
    Mockito.when(determiner.determine(nodeC)).thenReturn(true);
    Mockito.when(determiner.determine(nodeD)).thenReturn(true);
    Mockito.when(determiner.determine(nodeE)).thenReturn(true);
    Mockito.when(determiner.determine(nodeF)).thenReturn(true);
    Mockito.when(modelConfigService.getModelConfig().getCrossroadMinDistance()).thenReturn(10.0);
    repository.getComplexCrossroads().add(complexCrossroad1);
    repository.getComplexCrossroads().add(complexCrossroad2);

    // then
    updater.extendWithNodes(Set.of(nodeA, nodeC, nodeE, nodeF));
    Assertions.assertEquals(1, repository.getComplexCrossroads().size());
    Assertions.assertTrue(repository.getComplexCrossroads()
        .stream()
        .allMatch(cc -> cc.getNodesIdsIn().contains(nodeE.getId()) && cc.getNodesIdsIn().contains(nodeF.getId())));
  }
}
