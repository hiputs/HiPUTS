package pl.edu.agh.hiputs.partition.service.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class HexagonGridTest {

  private static Stream<Entry<CartesianCoordinate, HexagonCoordinate>> pointsMapping() {
    Map<CartesianCoordinate, HexagonCoordinate> expectedPointMapping = new HashMap<>();
    expectedPointMapping.put(new CartesianCoordinate(0.1, 0.2), new HexagonCoordinate(0, 0));
    expectedPointMapping.put(new CartesianCoordinate(0.0, 0.999), new HexagonCoordinate(0, 1));
    expectedPointMapping.put(new CartesianCoordinate(1.22, 1.37), new HexagonCoordinate(1, 0));
    expectedPointMapping.put(new CartesianCoordinate(0.46, 1.033), new HexagonCoordinate(0, 1));
    expectedPointMapping.put(new CartesianCoordinate(1.14, 2.32), new HexagonCoordinate(1, 1));
    expectedPointMapping.put(new CartesianCoordinate(3.40, 4.25), new HexagonCoordinate(2, 2));

    expectedPointMapping.put(new CartesianCoordinate(2.34, 3.12), new HexagonCoordinate(2, 2));
    expectedPointMapping.put(new CartesianCoordinate(2.96, 2.73), new HexagonCoordinate(2, 2));
    expectedPointMapping.put(new CartesianCoordinate(3.60, 3.02), new HexagonCoordinate(2, 2));
    expectedPointMapping.put(new CartesianCoordinate(3.67, 3.84), new HexagonCoordinate(2, 2));
    expectedPointMapping.put(new CartesianCoordinate(3.02, 4.22), new HexagonCoordinate(2, 2));
    expectedPointMapping.put(new CartesianCoordinate(2.35, 3.87), new HexagonCoordinate(2, 2));

    expectedPointMapping.put(new CartesianCoordinate(0.80, 2.23), new HexagonCoordinate(1, 1));
    expectedPointMapping.put(new CartesianCoordinate(1.51, 1.84), new HexagonCoordinate(1, 1));
    expectedPointMapping.put(new CartesianCoordinate(2.15, 2.25), new HexagonCoordinate(1, 1));
    expectedPointMapping.put(new CartesianCoordinate(2.14, 2.97), new HexagonCoordinate(1, 1));
    expectedPointMapping.put(new CartesianCoordinate(1.48, 3.34), new HexagonCoordinate(1, 1));
    expectedPointMapping.put(new CartesianCoordinate(0.81, 2.93), new HexagonCoordinate(1, 1));

    expectedPointMapping.put(new CartesianCoordinate(2.368, 2.603), new HexagonCoordinate(1, 1));
    expectedPointMapping.put(new CartesianCoordinate(1.6, 2.6), new HexagonCoordinate(1, 1));
    expectedPointMapping.put(new CartesianCoordinate(5.0, 0.0), new HexagonCoordinate(4, 0));

    return expectedPointMapping.entrySet().stream();
  }

  @ParameterizedTest
  @MethodSource("pointsMapping")
  public void getHexagonCoordinateTest(Entry<CartesianCoordinate, HexagonCoordinate> testData) {
    HexagonGrid hexagonGrid = new HexagonGrid(0.0, 0.0, 1.0);
    HexagonCoordinate hexagonCoordinate =
        hexagonGrid.getHexagonCoordinate(testData.getKey().getX(), testData.getKey().getY());
    Assertions.assertThat(hexagonCoordinate).isEqualTo(testData.getValue());
  }

  private static final double eps = 0.0001;

  private static Stream<Entry<List<Integer>, StandardEquationLine>> hexagonsWithExpectedBetweenLine() {
    Map<List<Integer>, StandardEquationLine> expectedLinesForHexCoordinates = new HashMap<>();
    expectedLinesForHexCoordinates.put(List.of(1, 1, 1, 0), new StandardEquationLine(0.0, -1.0, 1.7321));
    expectedLinesForHexCoordinates.put(List.of(1, 1, 1, 2), new StandardEquationLine(0.0, -1.0,3.4641));
    expectedLinesForHexCoordinates.put(List.of(1, 1, 0, 1), new StandardEquationLine(-Math.sqrt(3), -1.0, 3.4641));
    expectedLinesForHexCoordinates.put(List.of(1, 1, 0, 2), new StandardEquationLine(Math.sqrt(3), -1.0, 1.7321));
    expectedLinesForHexCoordinates.put(List.of(1, 1, 2, 1), new StandardEquationLine(Math.sqrt(3), -1.0, -1.7321));
    expectedLinesForHexCoordinates.put(List.of(1, 1, 2, 2), new StandardEquationLine(-Math.sqrt(3), -1.0, 6.9282));
    return expectedLinesForHexCoordinates.entrySet().stream();
  }

  @ParameterizedTest
  @MethodSource("hexagonsWithExpectedBetweenLine")
  public void getLineBetweenTest1(Entry<List<Integer>, StandardEquationLine> testData) {
    HexagonGrid hexagonGrid = new HexagonGrid(0.0, 0.0, 1.0);
    HexagonCoordinate c1 =
        HexagonCoordinate.builder().xHex(testData.getKey().get(0)).yHex(testData.getKey().get(1)).build();
    HexagonCoordinate c2 =
        HexagonCoordinate.builder().xHex(testData.getKey().get(2)).yHex(testData.getKey().get(3)).build();

    Assertions.assertThat((hexagonGrid.getLineBetween(c1, c2))).isNotNull();
    Assertions.assertThat(hexagonGrid.getLineBetween(c1, c2).getA()).isEqualTo(testData.getValue().getA());
    Assertions.assertThat(hexagonGrid.getLineBetween(c1, c2).getB())
        .isCloseTo(testData.getValue().getB(), Offset.offset(eps));
    Assertions.assertThat(hexagonGrid.getLineBetween(c1, c2).getC())
        .isCloseTo(testData.getValue().getC(), Offset.offset(eps));
  }

  @ParameterizedTest
  @MethodSource("hexagonsWithExpectedBetweenLine")
  public void getLineBetweenTest2(Entry<List<Integer>, StandardEquationLine> testData) {
    HexagonGrid hexagonGrid = new HexagonGrid(0.0, 0.0, 1.0);
    HexagonCoordinate c1 =
        HexagonCoordinate.builder().xHex(testData.getKey().get(0)).yHex(testData.getKey().get(1)).build();
    HexagonCoordinate c2 =
        HexagonCoordinate.builder().xHex(testData.getKey().get(2)).yHex(testData.getKey().get(3)).build();

    Assertions.assertThat(hexagonGrid.getLineBetween(c1, c2)).isEqualTo(hexagonGrid.getLineBetween(c2, c1));
  }


  @Data
  @AllArgsConstructor
  private static class CartesianCoordinate {

    private double x;
    private double y;
  }
}
