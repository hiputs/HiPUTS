package pl.edu.agh.hiputs.partition.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.edu.agh.hiputs.partition.mapper.util.successor.allocator.OnBendSuccessorAllocator;
import pl.edu.agh.hiputs.partition.mapper.util.successor.allocator.OnCrossroadSuccessorAllocator;
import pl.edu.agh.hiputs.partition.mapper.util.successor.pairing.DefaultPairingIncomingWithOutgoings;
import pl.edu.agh.hiputs.partition.mapper.util.transformer.GraphCrossroadDeterminer;
import pl.edu.agh.hiputs.partition.mapper.util.transformer.GraphLanesCreator;
import pl.edu.agh.hiputs.partition.mapper.util.transformer.GraphLengthFiller;
import pl.edu.agh.hiputs.partition.mapper.util.transformer.GraphMaxSpeedFiller;
import pl.edu.agh.hiputs.partition.mapper.util.transformer.GraphNextLanesAllocator;
import pl.edu.agh.hiputs.partition.mapper.util.transformer.GraphReverseRoadsCreator;
import pl.edu.agh.hiputs.partition.mapper.util.transformer.LargestCCSelector;
import pl.edu.agh.hiputs.partition.mapper.Osm2InternalModelMapper;
import pl.edu.agh.hiputs.partition.mapper.Osm2InternalModelMapperImpl;
import pl.edu.agh.hiputs.partition.mapper.util.oneway.StandardOsmAndRoundaboutOnewayProcessor;
import pl.edu.agh.hiputs.partition.mapper.util.turn.mapper.FixedAngleRangeTurnMapper;
import pl.edu.agh.hiputs.partition.mapper.util.turn.processor.StandardOsmTurnProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.partition.osm.OsmGraphReader;
import pl.edu.agh.hiputs.partition.osm.OsmGraphReaderImpl;
import pl.edu.agh.hiputs.partition.service.HexagonsPartitioner.BorderEdgesHandlingStrategy;
import pl.edu.agh.hiputs.partition.service.util.GraphPreconditionsChecker;

public class HexagonGridPartitionerTest {

  private double carViewRange;
  private static final double eps = 0.0001;
  private final OsmGraphReader osmGraphReader = new OsmGraphReaderImpl();
  private final Osm2InternalModelMapper osm2InternalModelMapper = new Osm2InternalModelMapperImpl(
      new StandardOsmAndRoundaboutOnewayProcessor(),
      List.of(
          new LargestCCSelector(),
          new GraphMaxSpeedFiller(),
          new GraphLengthFiller(),
          new GraphReverseRoadsCreator(),
          new GraphCrossroadDeterminer(),
          new GraphLanesCreator(new StandardOsmAndRoundaboutOnewayProcessor()),
          new GraphNextLanesAllocator(List.of(
              new OnBendSuccessorAllocator(new StandardOsmTurnProcessor()),
              new OnCrossroadSuccessorAllocator(
                  new DefaultPairingIncomingWithOutgoings(),
                  new StandardOsmTurnProcessor(),
                  new FixedAngleRangeTurnMapper()
              )
          ))
      )
  );


  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"minimalMap.osm", "straightTwoWayRoad.osm"})
  public void partitionWithMaxLaneLengthBuffer(String mapFileName) {
    carViewRange = 200.0;
    partitionTest(mapFileName, BorderEdgesHandlingStrategy.maxLaneLengthBuffer);
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"minimalMap.osm", "straightTwoWayRoad.osm"})
  public void partitionWithEdgeCutting(String mapFileName) {
    carViewRange = 4500.0;
    partitionTest(mapFileName, BorderEdgesHandlingStrategy.edgeCutting);
  }

  private void partitionTest(String mapFileName, BorderEdgesHandlingStrategy strategy) throws IOException {
    //given
    OsmGraph osmGraph = osmGraphReader.loadOsmData(Files.newInputStream(getResourcePath(mapFileName)));
    Graph<JunctionData, WayData> mapGraph = osm2InternalModelMapper.mapToInternalModel(osmGraph);
    PatchPartitioner patchPartitioner = new HexagonsPartitioner(strategy, carViewRange);

    //when
    Graph<PatchData, PatchConnectionData> patchesGraph = patchPartitioner.partition(mapGraph);

    //then
    PatchCorrectnessChecker patchCorrectnessChecker = new PatchCorrectnessChecker(carViewRange - eps);
    boolean patchesAreCorrect = patchCorrectnessChecker.testAllPatches(patchesGraph, mapGraph);
    Assertions.assertThat(patchesAreCorrect).isTrue();
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"minimalMap.osm", "straightTwoWayRoad.osm"})
  public void partitionDoNotDestroyGraphCorrectness(String mapFileName) {
    //given
    OsmGraph osmGraph = osmGraphReader.loadOsmData(Files.newInputStream(getResourcePath(mapFileName)));
    Graph<JunctionData, WayData> mapGraph = osm2InternalModelMapper.mapToInternalModel(osmGraph);
    HexagonsPartitioner patchPartitioner = new HexagonsPartitioner(BorderEdgesHandlingStrategy.maxLaneLengthBuffer, carViewRange);

    //when
    patchPartitioner.colorGraph(mapGraph);

    //then
    assertDoesNotThrow(() -> GraphPreconditionsChecker.checkPreconditions(mapGraph));
  }

  private Path getResourcePath(String resourcePathFromResourcesDir) {
    return Paths.get("src","test","resources", resourcePathFromResourcesDir);
  }
}
