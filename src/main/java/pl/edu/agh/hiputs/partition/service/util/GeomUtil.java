package pl.edu.agh.hiputs.partition.service.util;

import java.awt.Point;
import java.util.Optional;

public class GeomUtil {

  public static Optional<Point.Double> calculateIntersectionPoint(
      Point.Double p1,
      Point.Double p2,
      StandardEquationLine line1
  ) {
    StandardEquationLine line2 = calculateStandardEquationLine(p1, p2);
    return calculateIntersectionPoint(line1, line2);
  }

  private static StandardEquationLine calculateStandardEquationLine(
      Point.Double p1,
      Point.Double p2) {
    return new StandardEquationLine(
        -(p2.y - p1.y),
         (p2.x - p1.x),
        -(p2.x - p1.x) * p1.y + (p2.y - p1.y) * p1.x
    );
  }

  private static Optional<Point.Double> calculateIntersectionPoint(
      StandardEquationLine line1,
      StandardEquationLine line2) {

    double w = line1.getA() * line2.getB() - line1.getB() * line2.getA();
    double wx = - line1.getC() * line2.getB() + line1.getB() * line2.getC();
    double wy = - line1.getA() * line2.getC() + line1.getC() * line2.getA();

    double x = wx / w;
    double y = wy / w;

    if (Double.isFinite(wx / w) || Double.isFinite(wy / w)) {
      return Optional.of(new Point.Double(x, y));
    }

    return Optional.empty();
  }

}
