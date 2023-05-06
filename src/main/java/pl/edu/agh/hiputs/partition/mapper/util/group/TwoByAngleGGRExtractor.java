package pl.edu.agh.hiputs.partition.mapper.util.group;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
  private final static double ANGLE_DIFF = 180.0;

  @Override
  public List<List<Edge<JunctionData, WayData>>> extract(List<Edge<JunctionData, WayData>> edges) {
    TreeMap<Double, Edge<JunctionData, WayData>> angle2EdgeMap = create(edges, edges.get(0));
    Set<Set<Edge<JunctionData, WayData>>> distinctExtractedGroups = new HashSet<>();

    angle2EdgeMap.forEach((angle, edge) ->
        Stream.of(angle2EdgeMap.floorEntry(angle + ANGLE_DIFF), angle2EdgeMap.ceilingEntry(angle + ANGLE_DIFF))
            .filter(Objects::nonNull)
            .filter(entry -> !entry.getValue().equals(edge))
            .filter(entry -> 150 < entry.getKey() && entry.getKey() < 210)
            .map(entry -> Map.entry(Math.abs(ANGLE_DIFF - entry.getKey()), entry.getValue()))
            .min(Comparator.comparingDouble(Entry::getKey))
            .ifPresentOrElse(
                entry -> distinctExtractedGroups.add(Set.of(edge, entry.getValue())),
                () -> distinctExtractedGroups.add(Set.of(edge))
            )
    );

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
}
