package pl.edu.agh.hiputs.partition.model.geom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HexagonGrid {

  private static final double ROOT_THREE = Math.sqrt(3);
  private final Double originX;
  private final Double originY;
  private final Double a;

  public HexagonCoordinate getHexagonCoordinate(double x, double y) {
    return getHexagonCoordinateOnRelativeCoords(x - originX, y - originY);
  }

  public HexagonCoordinate getNeighbourHexagonCoordinate(HexagonCoordinate current, HexagonLineSegmentOrientation neighbourOrientation) {
    switch (neighbourOrientation) {
      case UP_LEFT -> {
        return new HexagonCoordinate(current.getXHex() - 1, current.getYHex() + current.getXHex() % 2);
      }
      case UP -> {
        return new HexagonCoordinate(current.getXHex(), current.getYHex() + 1);
      }
      case UP_RIGHT -> {
        return new HexagonCoordinate(current.getXHex() + 1, current.getYHex() + current.getXHex() % 2);
      }
      case DOW_RIGHT -> {
        return new HexagonCoordinate(current.getXHex() + 1, current.getYHex() + current.getXHex() % 2 - 1);
      }
      case DOWN -> {
        return new HexagonCoordinate(current.getXHex(), current.getYHex() - 1);
      }
      case DOWN_LEFT -> {
        return new HexagonCoordinate(current.getXHex() - 1, current.getYHex() + current.getXHex() % 2 - 1);
      }
    }
    return null;
  }

  public StandardEquationLine getLineBetween(HexagonCoordinate c1, HexagonCoordinate c2) {
    StandardEquationLine line = getLineBetweenRelativeCoords(c1, c2);
    return line == null ? null : new StandardEquationLine(line.getA(), line.getB(), line.getC() + originY - line.getA()*originX);
  }

  public boolean areNeighbours(HexagonCoordinate c1, HexagonCoordinate c2) {
    int x = c1.getXHex();
    int y = c1.getYHex();
    List<HexagonCoordinate> c1Neighbours = List.of(
        new HexagonCoordinate(x - 1, y + x % 2),
        new HexagonCoordinate(x - 1, y + x % 2 - 1),
        new HexagonCoordinate(x, y + 1),
        new HexagonCoordinate(x, y - 1),
        new HexagonCoordinate(x + 1, y + x % 2),
        new HexagonCoordinate(x + 1, y + x % 2 - 1)
    );
    return c1Neighbours.contains(c2);
  }

  public List<HexagonLineSegment> getLineSegmentsOfHexagon(HexagonCoordinate c) {
    List<Point> sortedHexagonPointsByCentralAngle = List.of(
        getCentralPointOfHexagon(c).translate(-a, 0),
        getCentralPointOfHexagon(c).translate(-a/2, a*ROOT_THREE/2),
        getCentralPointOfHexagon(c).translate(a/2, a*ROOT_THREE/2),
        getCentralPointOfHexagon(c).translate(a, 0),
        getCentralPointOfHexagon(c).translate(a/2, -a*ROOT_THREE/2),
        getCentralPointOfHexagon(c).translate(-a/2, -a*ROOT_THREE/2),
        getCentralPointOfHexagon(c).translate(-a, 0)
        );

    List<HexagonLineSegment> lineSegments = new LinkedList<>();
    for(int i=0; i<6; i++) {
      lineSegments.add(new HexagonLineSegment(sortedHexagonPointsByCentralAngle.get(i), sortedHexagonPointsByCentralAngle.get(i+1), HexagonLineSegmentOrientation.valueOf(i)));
    }

    return lineSegments;
  }

  private Point getCentralPointOfHexagon(HexagonCoordinate c) {
    return new Point(originX + c.getXHex()*3*a/2, originY + c.getYHex()*a*ROOT_THREE + c.getXHex()%2*a*ROOT_THREE/2);
  }

  private StandardEquationLine getLineBetweenRelativeCoords(HexagonCoordinate c1, HexagonCoordinate c2) {
    //horizontal line
    if (c1.getXHex() == c2.getXHex()) {
      if (c1.getYHex() == c2.getYHex() + 1) {
        //up for c2
        return getUpBoundaryLineFor(c2);
      } else if (c1.getYHex() == c2.getYHex() - 1) {
        // up for c1
        return getUpBoundaryLineFor(c1);
      }
    }

    //c1 should have lower x
    if (c1.getXHex() > c2.getXHex()) {
      HexagonCoordinate tmp = c2;
      c2 = c1;
      c1 = tmp;
    }

    //up right for c1
    if (c1.getXHex() % 2 == 0 && c1.getYHex() == c2.getYHex()) {
        return getUpRightBoundaryLineFor(c1);
    }
    //up left for c2
    if (c1.getXHex() % 2 == 0 && c1.getYHex() == c2.getYHex() + 1) {
        return getUpLeftBoundaryLineFor(c2);
    }
    //up left for c2
    if (c1.getXHex() % 2 == 1 && c1.getYHex() == c2.getYHex()) {
        return getUpLeftBoundaryLineFor(c2);
    }
    // up right for c1
    if (c1.getXHex() % 2 == 1 && c1.getYHex() == c2.getYHex() - 1) {
        return getUpRightBoundaryLineFor(c1);
    }

    return null;
  }

  private StandardEquationLine getUpBoundaryLineFor(HexagonCoordinate c) {
    double intercept = (c.getXHex() % 2) * a * ROOT_THREE / 2 + a * ROOT_THREE * c.getYHex() + a * ROOT_THREE / 2;
    return new StandardEquationLine(0, -1.0, intercept);
  }

  private StandardEquationLine getUpLeftBoundaryLineFor(HexagonCoordinate c) {
    double intercept = - 3 * ROOT_THREE * a * c.getXHex() / 2 + ROOT_THREE * a * (c.getXHex() % 2) / 2 + a * ROOT_THREE * c.getYHex() + a * ROOT_THREE;
    return new StandardEquationLine(ROOT_THREE, -1.0, intercept);
  }

  private StandardEquationLine getUpRightBoundaryLineFor(HexagonCoordinate c) {
    double intercept = 3 * ROOT_THREE * a * c.getXHex() / 2 + ROOT_THREE * a * (c.getXHex() % 2) / 2 + a * ROOT_THREE * c.getYHex() + a * ROOT_THREE;
    return new StandardEquationLine(-ROOT_THREE, -1.0, intercept);
  }

  private HexagonCoordinate getHexagonCoordinateOnRelativeCoords(double x, double y) {
    return getHexagonCoordinateAssumingIMod(x, y, 0).orElse(getHexagonCoordinateAssumingIMod(x, y, 1).orElse(null));
  }

  private Optional<HexagonCoordinate> getHexagonCoordinateAssumingIMod(double x, double y, int iMod2) {
    int j = (int) (Math.floor(y / (ROOT_THREE * a) + 0.5 - ((double) iMod2) / 2));
    double iMaxBound1 =
        -2 * y / (3 * a * ROOT_THREE) + 2 * x / (3 * a) + ((double) iMod2) / 3 + 2 * ((double) j) / 3 + 2.0 / 3;
    double iMinBound1 = iMaxBound1 - 4.0 / 3;
    double iMaxBound2 =
        2 * y / (3 * a * ROOT_THREE) + 2 * x / (3 * a) - ((double) iMod2) / 3 - 2 * ((double) j) / 3 + 2.0 / 3;
    double iMinBound2 = iMaxBound2 - 4.0 / 3;

    List<Integer> iCandidates = getIntegersInIntersection(iMinBound1, iMaxBound1, iMinBound2, iMaxBound2).stream()
        .filter(i -> i % 2 == iMod2)
        .toList();

    if (iCandidates.size() > 1) {
      throw new IllegalStateException("Too many candidates for i");
    }

    return iCandidates.stream().findAny().map(i -> new HexagonCoordinate(i, j));
  }

  private List<Integer> getIntegersInIntersection(double aStart, double aEnd, double bStart, double bEnd) {
    if (aStart > bEnd || bStart > aEnd) {
      return List.of();
    }
    double intersectionStart = Math.max(aStart, bStart);
    double intersectionEnd = Math.min(aEnd, bEnd);

    List<Integer> result = new ArrayList<>();
    int i = (int) Math.ceil(intersectionStart);
    while (i <= intersectionEnd) {
      result.add(i);
      i++;
    }
    return result;
  }

  @Getter
  public static class HexagonLineSegment extends LineSegment {

    private final HexagonLineSegmentOrientation orientation;

    public HexagonLineSegment(Point p1, Point p2, HexagonLineSegmentOrientation orientation) {
      super(p1, p2);
      this.orientation = orientation;
    }
  }

  public enum HexagonLineSegmentOrientation {
    UP_LEFT(0),
    UP(1),
    UP_RIGHT(2),
    DOW_RIGHT(3),
    DOWN(4),
    DOWN_LEFT(5);

    private final int i;
    HexagonLineSegmentOrientation(int i) {
      this.i = i;
    }

    public static HexagonLineSegmentOrientation valueOf(int val) {
      return Arrays.stream(HexagonLineSegmentOrientation.values())
          .filter(e -> e.i == val).findAny().orElseThrow();
    }
  }
}
