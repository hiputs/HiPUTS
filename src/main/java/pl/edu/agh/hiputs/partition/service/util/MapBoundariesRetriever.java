package pl.edu.agh.hiputs.partition.service.util;

import java.util.Comparator;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.NotImplementedException;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;

public class MapBoundariesRetriever {

  public static MapBoundaries retrieveMapBoundaries(Graph<JunctionData, WayData> graph) {
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

    return MapBoundaries.builder()
        .leftBottomPlanarX(minX)
        .leftBottomPlanarY(minY)
        .width(maxX - minX)
        .height(maxY - minY)
        .build();
  }

  @Data
  @Builder
  public static class MapBoundaries {

    private double width;
    private double height;
    private double leftBottomPlanarX;
    private double leftBottomPlanarY;
  }

}
