package pl.edu.agh.hiputs.partition.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.service.util.HexagonCoordinate;
import pl.edu.agh.hiputs.partition.service.util.HexagonGrid;
import pl.edu.agh.hiputs.partition.service.util.MapBoundariesRetriever;
import pl.edu.agh.hiputs.partition.service.util.MapBoundariesRetriever.MapBoundaries;
import pl.edu.agh.hiputs.partition.service.util.PatchesGraphExtractor;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;

@Slf4j
@RequiredArgsConstructor
public class HexagonsPartitioner implements PatchPartitioner {

  @NonNull
  private final BorderEdgesHandlingStrategy borderEdgesHandlingStrategy;
  private final double carViewRange;

  @Override
  public Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph) {
    MapBoundaries mapBoundaries = retrieveMapBoundaries(graph);

    HexagonGrid hexagonGrid = new HexagonGrid(mapBoundaries.getLeftBottomPlanarX(), mapBoundaries.getLeftBottomPlanarY(), carViewRange);
    graph.getNodes().values()
        .forEach(node -> {
          double x = CoordinatesUtil.longitude2plain(node.getData().getLon(), node.getData().getLat());
          double y = CoordinatesUtil.latitude2plain(node.getData().getLat());
          HexagonCoordinate hexagonCoordinate = hexagonGrid.getHexagonCoordinate(x, y);
          String patchId = String.format("%d%d", hexagonCoordinate.getXHex(), hexagonCoordinate.getYHex());
          node.getData().setPatchId(patchId);
          node.getIncomingEdges().forEach(edge -> edge.getData().setPatchId(patchId));
        });

    Graph<PatchData, PatchConnectionData> patchesGraph = new PatchesGraphExtractor().createFrom(graph);
    log.info("Partitioning into patches finished");
    return patchesGraph;
  }

  private MapBoundaries retrieveMapBoundaries(Graph<JunctionData, WayData> graph) {
    return MapBoundariesRetriever.retrieveMapBoundaries(graph);
  }

  public enum BorderEdgesHandlingStrategy {
    edgeCutting,
    maxLaneLengthBuffer,
    hybrid
  }
}
