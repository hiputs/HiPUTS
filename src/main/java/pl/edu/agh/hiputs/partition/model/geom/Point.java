package pl.edu.agh.hiputs.partition.model.geom;

import java.awt.geom.Point2D;

public class Point {

  private final Point2D.Double point;

  public Point(double x, double y) {
    this.point = new Point2D.Double(x, y);
  }

  public double getX() {
    return point.getX();
  }

  public double getY() {
    return point.getY();
  }

  public Point translate(double vx, double vy) {
    return new Point(point.getX() + vx, point.getY() + vy);
  }

  public double distanceTo(Point other) {
    return point.distance(other.point);
  }

}
