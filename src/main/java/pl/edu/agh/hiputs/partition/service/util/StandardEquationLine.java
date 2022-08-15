package pl.edu.agh.hiputs.partition.service.util;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Line given by standard equation: Ax + By + C = 0
 */
@Data
@AllArgsConstructor
public class StandardEquationLine {

  private double A;
  private double B;
  private double C;

  public Optional<Point> intersectionPointWith(StandardEquationLine other) {
    double w = this.getA() * other.getB() - this.getB() * other.getA();
    double wx = - this.getC() * other.getB() + this.getB() * other.getC();
    double wy = - this.getA() * other.getC() + this.getC() * other.getA();

    double x = wx / w;
    double y = wy / w;

    if (java.lang.Double.isFinite(wx / w) || java.lang.Double.isFinite(wy / w)) {
      return Optional.of(new Point(x, y));
    }

    return Optional.empty();
  }
}
