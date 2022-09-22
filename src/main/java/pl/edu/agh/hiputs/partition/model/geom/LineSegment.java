package pl.edu.agh.hiputs.partition.model.geom;

import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.util.Optional;

public class LineSegment {

  private final Line2D line;

  public LineSegment(Point p1, Point p2) {
    this.line = new Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());
  }

  public Point getP1() {
    return new Point(line.getX1(), line.getY1());
  }

  public Point getP2() {
    return new Point(line.getX2(), line.getY2());
  }

  public StandardEquationLine getStandardEquationLine() {
    Point p1 = this.getP1();
    Point p2 = this.getP2();
    return new StandardEquationLine(
        -(p2.getY() - p1.getY()),
        (p2.getX() - p1.getX()),
        -(p2.getX() - p1.getX()) * p1.getY() + (p2.getY() - p1.getY()) * p1.getX()
    );
  }

  public Optional<Point> intersectionPointWith(LineSegment lineSegment) {
    if(!this.line.intersectsLine(lineSegment.line)) {
      return Optional.empty();
    }
    return this.intersectionPointWith(lineSegment.getStandardEquationLine());
  }


  public Optional<Point> intersectionPointWith(StandardEquationLine other) {
    return this.getStandardEquationLine().intersectionPointWith(other);
  }

}
