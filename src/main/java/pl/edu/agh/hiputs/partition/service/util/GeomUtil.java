package pl.edu.agh.hiputs.partition.service.util;

import java.awt.Point;
import java.util.Optional;

public class GeomUtil {

  public static Optional<Point.Double> calculateIntersectionPoint(
      Point.Double p1,
      Point.Double p2,
      SlopeInterceptLine line1
  ) {
    SlopeInterceptLine line2 = calculateSlopeInterceptLine(p1, p2);
    return calculateIntersectionPoint(line1, line2);
  }

  private static SlopeInterceptLine calculateSlopeInterceptLine(
      Point.Double p1,
      Point.Double p2) {
    return new SlopeInterceptLine(
        (p1.y - p2.y) / (p1.x - p2.x),
        p1.y - p1.x * (p1.y - p2.y) / (p1.x - p2.x)
    );
  }

  private static Optional<Point.Double> calculateIntersectionPoint(
      SlopeInterceptLine line1,
      SlopeInterceptLine line2) {

    if (line1.getSlope() == line2.getSlope()) {
      return Optional.empty();
    }

    double x = (line2.getIntercept() - line1.getIntercept()) / (line1.getSlope() - line2.getSlope());
    double y = line1.getSlope() * x + line1.getIntercept();

    return Optional.of(new Point.Double(x, y));
  }

}
