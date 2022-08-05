package pl.edu.agh.hiputs.partition.service;

import java.util.Comparator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.service.util.HexagonCoordinate;
import pl.edu.agh.hiputs.partition.service.util.HexagonGrid;
import pl.edu.agh.hiputs.partition.service.util.PatchesGraphExtractor;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;

@Slf4j
public class HexagonsPartitioner implements PatchPartitioner {

  private MapBoundaries mapBoundaries;

  @Override
  public Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph) {
    retrieveMapBoundaries(graph);

    HexagonGrid hexagonGrid = new HexagonGrid(mapBoundaries.leftBottomPlanarX, mapBoundaries.leftBottomPlanarY, 100.0);
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

  private void retrieveMapBoundaries(Graph<JunctionData, WayData> graph) {
    double maxLon = graph.getNodes().values().stream().max(Comparator.comparing(n -> n.getData().getLon())).get().getData().getLon();
    double minLon = graph.getNodes().values().stream().min(Comparator.comparing(n -> n.getData().getLon())).get().getData().getLon();

    double maxLat = graph.getNodes().values().stream().max(Comparator.comparing(n -> n.getData().getLat())).get().getData().getLat();
    double minLat = graph.getNodes().values().stream().min(Comparator.comparing(n -> n.getData().getLat())).get().getData().getLat();

    //południk 180
    if (maxLon * minLon < 0) {
      throw new NotImplementedException("Case on lon 180/-180 is not supported yet");
    }

    //równik i kształt trapezu
    if (maxLat * minLat < 0) {
      throw new NotImplementedException("Case on lat ~0 is not supported yet");
    }

    double minX = Math.min(CoordinatesUtil.longitude2plain(minLon, minLat), CoordinatesUtil.longitude2plain(minLon, maxLat));
    double minY = CoordinatesUtil.latitude2plain(minLat);
    double maxX = Math.max(CoordinatesUtil.longitude2plain(maxLon, minLat), CoordinatesUtil.longitude2plain(maxLon, maxLat));
    double maxY = CoordinatesUtil.latitude2plain(maxLat);

    mapBoundaries = new MapBoundaries();
    mapBoundaries.setLeftBottomPlanarX(minX);
    mapBoundaries.setLeftBottomPlanarY(minY);
    mapBoundaries.setWidth(maxX - minX);
    mapBoundaries.setHeight(maxY - minY);
  }

  @Data
  public static class MapBoundaries {
    private double width;
    private double height;
    private double leftBottomPlanarX;
    private double leftBottomPlanarY;
  }
}
