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

  public SlopeInterceptLine getLineBetween(HexagonCoordinate c1, HexagonCoordinate c2) {
    //horizontal line
    if (c1.getXHex() == c2.getXHex()) {
      if (c1.getYHex() == c2.getYHex() + 1) {
        //up for c2
        return getUpBoundaryLineFor(c2);
      } else if (c1.getYHex() == c2.getYHex() - 1) {
        // up for c1
        return getUpBoundaryLineFor(c1);
      } else {
        return null;
      }
    }

    //c1 should have lower x
    if (c1.getXHex() > c2.getXHex()) {
      HexagonCoordinate tmp = c2;
      c2 = c1;
      c1 = tmp;
    }

    //
    if (c1.getXHex() % 2 == 0) {
      if (c1.getYHex() == c2.getYHex()) {
        //up right for c1
        return getUpRightBoundaryLineFor(c1);
      } else if (c1.getYHex() == c2.getYHex() + 1) {
        //up left for c2
        return getUpLeftBoundaryLineFor(c2);
      }
    } else {
      if (c1.getYHex() == c2.getYHex()) {
        //up left for c2
        return getUpLeftBoundaryLineFor(c2);
      } else if (c1.getYHex() == c2.getYHex() - 1) {
        // up right for c1
        return getUpRightBoundaryLineFor(c1);
      }
    }
    return null;
  }

  private SlopeInterceptLine getUpBoundaryLineFor(HexagonCoordinate c) {
    double intercept = (c.getXHex() % 2) * a * ROOT_THREE / 2 + a * ROOT_THREE * c.getYHex() + a * ROOT_THREE / 2;
    return new SlopeInterceptLine(0, intercept);
  }

  private SlopeInterceptLine getUpLeftBoundaryLineFor(HexagonCoordinate c) {
    double intercept = - 3 * ROOT_THREE * a * c.getXHex() / 2 + ROOT_THREE * a * (c.getXHex() % 2) / 2 + a * ROOT_THREE * c.getYHex() + a * ROOT_THREE;
    return new SlopeInterceptLine(ROOT_THREE, intercept);
  }

  private SlopeInterceptLine getUpRightBoundaryLineFor(HexagonCoordinate c) {
    double intercept = 3 * ROOT_THREE * a * c.getXHex() / 2 + ROOT_THREE * a * (c.getXHex() % 2) / 2 + a * ROOT_THREE * c.getYHex() + a * ROOT_THREE;
    return new SlopeInterceptLine(-ROOT_THREE, intercept);
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
