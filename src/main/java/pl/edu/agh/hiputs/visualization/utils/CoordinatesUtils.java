package pl.edu.agh.hiputs.visualization.utils;

import proto.model.Coordinates;
import proto.model.VisualizationStateChangeMessage.ROIRegion;

public class CoordinatesUtils {

  public static boolean isCoordinateEmpty(Coordinates coordinates) {
    return Coordinates.getDefaultInstance().equals(coordinates);
  }

  public static boolean isRegionEmpty(ROIRegion roiRegion) {
    return ROIRegion.getDefaultInstance().equals(roiRegion);
  }

}
