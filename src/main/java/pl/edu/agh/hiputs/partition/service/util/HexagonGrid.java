package pl.edu.agh.hiputs.partition.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

  public StandardEquationLine getLineBetween(HexagonCoordinate c1, HexagonCoordinate c2) {
    StandardEquationLine line = getLineBetweenRelativeCoords(c1, c2);
    return line == null ? null : new StandardEquationLine(line.getA(), line.getB(), line.getC() + originY - line.getA()*originX);
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
}
