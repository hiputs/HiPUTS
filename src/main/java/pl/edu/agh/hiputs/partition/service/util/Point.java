package pl.edu.agh.hiputs.partition.service.util;

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

}
