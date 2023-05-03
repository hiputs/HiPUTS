package pl.edu.agh.hiputs.partition.model.geom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClockwiseSortingTest {
  private Point center = new Point(0, 0);
  private Point start = new Point(1, 1);

  @Test
  public void sortOnePoint() {
    // given
    ClockwiseSorting<Integer> sorter = new ClockwiseSorting<>(false);
    List<Pair<Point, Integer>> points = Stream.of(Pair.of(start, 1)).collect(Collectors.toList());

    // when
    sorter.sortByPointsWithRef(points, center, start);

    // then
    Assertions.assertEquals(start, points.get(0).getLeft());
  }

  @Test
  public void sortMorePoints() {
    // given
    ClockwiseSorting<Integer> sorter = new ClockwiseSorting<>(false);
    Point point1 = new Point(1, -1);
    Point point2 = new Point(-1, -1);
    Point point3 = new Point(-1, 1);
    List<Pair<Point, Integer>> points = Stream.of(
        Pair.of(point2, 3), Pair.of(start, 1), Pair.of(point3, 4), Pair.of(point1, 2)
    ).collect(Collectors.toList());

    // when
    sorter.sortByPointsWithRef(points, center, start);

    // then
    Assertions.assertEquals(start, points.get(0).getLeft());
    Assertions.assertEquals(point1, points.get(1).getLeft());
    Assertions.assertEquals(point2, points.get(2).getLeft());
    Assertions.assertEquals(point3, points.get(3).getLeft());
  }

  @Test
  public void sortMoreSortedPoints() {
    // given
    ClockwiseSorting<Integer> sorter = new ClockwiseSorting<>(false);
    Point point1 = new Point(1, -1);
    Point point2 = new Point(-1, -1);
    Point point3 = new Point(-1, 1);
    List<Pair<Point, Integer>> points = Stream.of(
        Pair.of(start, 1), Pair.of(point1, 2), Pair.of(point2, 3), Pair.of(point3, 4)
        ).collect(Collectors.toList());

    // when
    sorter.sortByPointsWithRef(points, center, start);

    // then
    Assertions.assertEquals(start, points.get(0).getLeft());
    Assertions.assertEquals(point1, points.get(1).getLeft());
    Assertions.assertEquals(point2, points.get(2).getLeft());
    Assertions.assertEquals(point3, points.get(3).getLeft());
  }

  @Test
  public void sortMorePointsInReverse() {
    // given
    ClockwiseSorting<Integer> sorter = new ClockwiseSorting<>(true);
    Point point1 = new Point(1, -1);
    Point point2 = new Point(-1, -1);
    Point point3 = new Point(-1, 1);
    List<Pair<Point, Integer>> points = Stream.of(
        Pair.of(point2, 3), Pair.of(start, 1), Pair.of(point3, 4), Pair.of(point1, 2)
    ).collect(Collectors.toList());

    // when
    sorter.sortByPointsWithRef(points, center, start);

    // then
    Assertions.assertEquals(start, points.get(3).getLeft());
    Assertions.assertEquals(point1, points.get(2).getLeft());
    Assertions.assertEquals(point2, points.get(1).getLeft());
    Assertions.assertEquals(point3, points.get(0).getLeft());
  }
}
