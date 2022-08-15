package pl.edu.agh.hiputs.partition.service.util;

import java.util.Optional;

public class GeomUtil {

  public static Optional<Point> calculateIntersectionPoint(
      Point p1,
      Point p2,
      StandardEquationLine line1
  ) {
    StandardEquationLine line2 = new LineSegment(p1, p2).getStandardEquationLine();
    return line1.intersectionPointWith(line2);
  }

}
