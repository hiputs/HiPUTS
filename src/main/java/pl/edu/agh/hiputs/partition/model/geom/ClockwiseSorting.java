package pl.edu.agh.hiputs.partition.model.geom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

@RequiredArgsConstructor
public class ClockwiseSorting<T> {
  private final boolean reverseOrder;

  public void sortByPointsWithRef(List<Pair<Point, T>> points, Point center, Point start) {
    Vector startVector = new Vector(center, start);

    List<Double> angles = new ArrayList<>(points.stream()
        .map(Pair::getLeft)
        .map(point -> Vector.calculateAngleBetween(startVector, new Vector(center, point)))
        .toList());

    quickSort(points, angles, 0, points.size() - 1);
  }

  private void quickSort(List<Pair<Point, T>> points, List<Double> angles, int low, int high) {
    if (low < high) {
      int pivotIndex = partition(points, angles, low, high);
      quickSort(points, angles, low, pivotIndex - 1);
      quickSort(points, angles, pivotIndex + 1, high);
    }
  }

  private int partition(List<Pair<Point, T>> points, List<Double> angles, int low, int high) {
    double pivot = angles.get(high);
    AtomicInteger i = new AtomicInteger(low);

    IntStream.range(low, high)
        .filter(index -> reverseOrder ? angles.get(index) > pivot : angles.get(index) < pivot)
        .forEach(index -> {
          Collections.swap(angles, i.get(), index);
          Collections.swap(points, i.get(), index);
          i.incrementAndGet();
        });

    Collections.swap(angles, i.get(), high);
    Collections.swap(points, i.get(), high);

    return i.get();
  }
}
