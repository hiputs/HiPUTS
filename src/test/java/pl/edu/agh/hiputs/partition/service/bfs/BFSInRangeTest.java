package pl.edu.agh.hiputs.partition.service.bfs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.Osm2InternalModelMapper;
import pl.edu.agh.hiputs.partition.mapper.Osm2InternalModelMapperImpl;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.partition.osm.OsmGraphReader;
import pl.edu.agh.hiputs.partition.osm.OsmGraphReaderImpl;

@Slf4j
public class BFSInRangeTest {

  private final OsmGraphReader osmGraphReader = new OsmGraphReaderImpl();

  private final Osm2InternalModelMapper osm2InternalModelMapper = new Osm2InternalModelMapperImpl();

  private Graph<JunctionData, WayData> testGraph;

  private final Measure<Edge<JunctionData, WayData>> testMeasure = new TestMeasure();

  private final Path osmFilePath = getResourcePath("straightTwoWayRoad.osm");

  @BeforeEach
  private void init() {
    InputStream is = null;
    try {
      is = Files.newInputStream(osmFilePath);
    } catch (IOException e) {
      log.error(e.getMessage());
    }
    OsmGraph osmGraph = osmGraphReader.loadOsmData(is);
    testGraph = osm2InternalModelMapper.mapToInternalModel(osmGraph);
  }

  @Test
  public void bfsInRangeTestCase1() {
    BFSWithRange<JunctionData, WayData> bfsInRange = new BFSWithRange<>(1000.0, testMeasure);
    Node<JunctionData, WayData> root = findNodeById("1000");

    BFSWithRangeResult<JunctionData, WayData> result = bfsInRange.getInRange(testGraph, root);
    Assertions.assertThat(result.getEdgesInRange().size()).isEqualTo(1);
    Assertions.assertThat(result.getBorderNodes().size()).isEqualTo(1);
  }

  @Test
  public void bfsInRangeTestCase2() {
    BFSWithRange<JunctionData, WayData> bfsInRange = new BFSWithRange<>(1500.0, testMeasure);
    Node<JunctionData, WayData> root = findNodeById("1000");
    log.info(String.format("Length of first lane is %f", root.getIncomingEdges().get(0).getData().getLength()));

    BFSWithRangeResult<JunctionData, WayData> result = bfsInRange.getInRange(testGraph, root);
    Assertions.assertThat(result.getEdgesInRange().size()).isEqualTo(3);
    Assertions.assertThat(result.getBorderNodes().size()).isEqualTo(1);
  }

  @Test
  public void bfsInRangeTestCase3() {
    BFSWithRange<JunctionData, WayData> bfsInRange = new BFSWithRange<>(2500.0, testMeasure);
    Node<JunctionData, WayData> root = findNodeById("1000");
    log.info(String.format("Length of first lane is %f", root.getIncomingEdges().get(0).getData().getLength()));

    BFSWithRangeResult<JunctionData, WayData> result = bfsInRange.getInRange(testGraph, root);
    Assertions.assertThat(result.getEdgesInRange().size()).isEqualTo(5);
    Assertions.assertThat(result.getBorderNodes().size()).isEqualTo(1);
  }

  @Test
  public void bfsInRangeTestCase4() {
    BFSWithRange<JunctionData, WayData> bfsInRange = new BFSWithRange<>(1000.0, testMeasure);
    Node<JunctionData, WayData> root = findNodeById("4444");
    log.info(String.format("Length of first lane is %f", root.getIncomingEdges().get(0).getData().getLength()));

    BFSWithRangeResult<JunctionData, WayData> result = bfsInRange.getInRange(testGraph, root);
    Assertions.assertThat(result.getEdgesInRange().size()).isEqualTo(2);
    Assertions.assertThat(result.getBorderNodes().size()).isEqualTo(2);
  }

  @Test
  public void bfsInRangeTestCase5() {
    BFSWithRange<JunctionData, WayData> bfsInRange = new BFSWithRange<>(1500.0, testMeasure);
    Node<JunctionData, WayData> root = findNodeById("4444");
    log.info(String.format("Length of first lane is %f", root.getIncomingEdges().get(0).getData().getLength()));

    BFSWithRangeResult<JunctionData, WayData> result = bfsInRange.getInRange(testGraph, root);
    Assertions.assertThat(result.getEdgesInRange().size()).isEqualTo(6);
    Assertions.assertThat(result.getBorderNodes().size()).isEqualTo(2);
  }

  @Test
  public void multipleRuns() {
    BFSWithRange<JunctionData, WayData> bfsInRange = new BFSWithRange<>(1500.0, testMeasure);
    Node<JunctionData, WayData> root = findNodeById("1111");

    BFSWithRangeResult<JunctionData, WayData> result = bfsInRange.getInRange(testGraph, root);
    Assertions.assertThat(result.getEdgesInRange().size()).isEqualTo(5);
    Assertions.assertThat(result.getBorderNodes().size()).isEqualTo(1);


    root = findNodeById("1000");

    result = bfsInRange.getInRange(testGraph, root);
    Assertions.assertThat(result.getEdgesInRange().size()).isEqualTo(3);
    Assertions.assertThat(result.getBorderNodes().size()).isEqualTo(1);
  }

  private Node<JunctionData, WayData> findNodeById(String id) {
    return testGraph.getNodes().get(id);
  }

  private Path getResourcePath(String resourcePathFromResourcesDir) {
    return Paths.get("src", "test", "resources", resourcePathFromResourcesDir);
  }

  private static class TestMeasure implements Measure<Edge<JunctionData, WayData>> {
    @Override
    public double measure(Edge<JunctionData, WayData> measured_object) {
      return measured_object.getData().getLength();
    }
  }
}
