package pl.edu.agh.hiputs.partition.mapper.util.group;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.geom.Point;
import pl.edu.agh.hiputs.partition.model.geom.Vector;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
public class TwoByAngleGGRExtractor implements GreenGroupRoadsExtractor, Angle2EdgeMapCreator{
  private final static double ANGLE_AMPLITUDE = 30.0;
  private final static double ANGLE_DIFF = 180.0;
  private final static double ANGLE_FULL = 360.0;

  @Override
  public List<List<Edge<JunctionData, WayData>>> extract(List<Edge<JunctionData, WayData>> edges) {
    if (edges.size() <= 2) {
      return edges.stream()
          .map(List::of)
          .collect(Collectors.toList());
    }

    TreeMap<Double, Edge<JunctionData, WayData>> angle2EdgeMapIter = create(edges, edges.get(0));
    TreeMap<Double, Edge<JunctionData, WayData>> angle2EdgeMapConsider = new TreeMap<>(angle2EdgeMapIter);
    Set<Set<Edge<JunctionData, WayData>>> distinctExtractedGroups = new HashSet<>();

    angle2EdgeMapIter.forEach((angle, edge) -> {
      if (angle2EdgeMapConsider.containsKey(angle) && angle2EdgeMapConsider.containsValue(edge)) {
        Stream.of(rollingFloorEntry(angle2EdgeMapConsider, angle + ANGLE_DIFF),
                rollingCeilingEntry(angle2EdgeMapConsider, angle + ANGLE_DIFF))
            .filter(Objects::nonNull)
            .filter(entry -> !entry.getValue().equals(edge))
            .filter(entry ->
                angle + ANGLE_DIFF - ANGLE_AMPLITUDE < entry.getKey() &&
                    entry.getKey() < angle + ANGLE_DIFF + ANGLE_AMPLITUDE)
            .min(Comparator.comparingDouble(entry -> Math.abs(ANGLE_DIFF - entry.getKey())))
            .ifPresentOrElse(entry -> {
              distinctExtractedGroups.add(Set.of(edge, entry.getValue()));
              angle2EdgeMapConsider.remove(entry.getKey());
              angle2EdgeMapConsider.remove(angle);
            }, () -> {
              distinctExtractedGroups.add(Set.of(edge));
              angle2EdgeMapConsider.remove(angle);
            });
      }
    });

    return distinctExtractedGroups.stream()
        .map(ArrayList::new)
        .collect(Collectors.toList());
  }

  @Override
  public TreeMap<Double, Edge<JunctionData, WayData>> create(
      List<Edge<JunctionData, WayData>> edges,
      Edge<JunctionData, WayData> refEdge
  ) {
    return edges.stream()
        .collect(Collectors.toMap(
            edge -> Vector.calculateAngleBetween(
                new Vector(
                    Point.convertFromCoords(refEdge.getTarget().getData()),
                    Point.convertFromCoords(refEdge.getSource().getData())),
                new Vector(
                    Point.convertFromCoords(edge.getTarget().getData()),
                    Point.convertFromCoords(edge.getSource().getData()))
            ),
            edge -> edge,
            (edge1, edge2) -> edge2,
            TreeMap::new
        ));
  }

  private Map.Entry<Double, Edge<JunctionData, WayData>> rollingFloorEntry(
      TreeMap<Double, Edge<JunctionData, WayData>> map, Double angle
  ) {
    Map.Entry<Double, Edge<JunctionData, WayData>> firstTry = map.floorEntry(angle);

    return firstTry == null ? map.floorEntry(angle + ANGLE_FULL) : firstTry;
  }

  private Map.Entry<Double, Edge<JunctionData, WayData>> rollingCeilingEntry(
      TreeMap<Double, Edge<JunctionData, WayData>> map, Double angle
  ) {
    Map.Entry<Double, Edge<JunctionData, WayData>> firstTry = map.ceilingEntry(angle);

    return firstTry == null ? map.ceilingEntry(angle - ANGLE_FULL) : firstTry;
  }
}
