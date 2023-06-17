package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.sort;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.geom.ClockwiseSorting;
import pl.edu.agh.hiputs.partition.model.geom.Point;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
public class ByAngleEdgeSorter implements EdgeSorter{
  // @TODO consider left-hand traffic
  private final ClockwiseSorting<Edge<JunctionData, WayData>> edgeSorter = new ClockwiseSorting<>(false);

  @Override
  public List<Edge<JunctionData, WayData>> getSorted(
      List<Edge<JunctionData, WayData>> edgesToSort,
      Edge<JunctionData, WayData> refEdge,
      Function<Edge<JunctionData, WayData>, JunctionData> appropriateNodeGetter
  ) {
    List<Pair<Point, Edge<JunctionData, WayData>>> dataToSort = edgesToSort.stream()
        .map(edge -> Pair.of(Point.convertFromCoords(appropriateNodeGetter.apply(edge)), edge))
        .collect(Collectors.toList());

    edgeSorter.sortByPointsWithRef(
        dataToSort,
        Point.convertFromCoords(refEdge.getTarget().getData()),
        Point.convertFromCoords(refEdge.getSource().getData())
    );

    return dataToSort.stream()
        .map(Pair::getRight)
        .collect(Collectors.toList());
  }
}
