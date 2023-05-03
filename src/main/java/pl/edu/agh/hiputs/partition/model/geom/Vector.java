package pl.edu.agh.hiputs.partition.model.geom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Vector {
  private final double x, y;

  public Vector(Point start, Point end) {
    this(end.getX() - start.getX(), end.getY() - start.getY());
  }

  public static double calculateAngleBetween(Vector first, Vector second) {
    double cosValue = dotProduct(first, second) / (first.norm() * second.norm());

    if (cosValue > 1) {
      cosValue = 1;
    } else if (cosValue < -1) {
      cosValue = -1;
    }

    double angle = Math.toDegrees(Math.acos(cosValue));
    return areInClockwiseOrder(first, second) ? angle : 360 - angle;
  }

  private static double dotProduct(Vector first, Vector second) {
    return first.x * second.x + first.y * second.y;
  }

  private static boolean areInClockwiseOrder(Vector first, Vector second) {
    return first.y * second.x - first.x * second.y >= 0;
  }

  private double norm() {
    return Math.sqrt(x*x + y*y);
  }
}
