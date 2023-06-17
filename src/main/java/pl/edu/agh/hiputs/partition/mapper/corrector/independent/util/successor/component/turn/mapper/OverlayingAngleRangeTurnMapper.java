package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.TurnDirection;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.geom.Point;
import pl.edu.agh.hiputs.partition.model.geom.Vector;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
@Primary
public class OverlayingAngleRangeTurnMapper implements TurnMapper{
  private final List<Pair<Pair<Double, Double>, TurnDirection>> angleRangesWithTurns = List.of(
      Pair.of(Pair.of(0.0, 90.0), TurnDirection.SHARP_LEFT),
      Pair.of(Pair.of(30.0, 150.0), TurnDirection.LEFT),
      Pair.of(Pair.of(90.0, 180.0), TurnDirection.SLIGHT_LEFT),
      Pair.of(Pair.of(120.0, 240.0), TurnDirection.THROUGH),
      Pair.of(Pair.of(180.0, 270.0), TurnDirection.SLIGHT_RIGHT),
      Pair.of(Pair.of(210.0, 330.0), TurnDirection.RIGHT),
      Pair.of(Pair.of(270.0, 360.0), TurnDirection.SHARP_RIGHT)
  );

  @Override
  public Map<TurnDirection, Edge<JunctionData, WayData>> assignTurns2OutgoingEdges(
      List<Edge<JunctionData, WayData>> outgoingEdges, Edge<JunctionData, WayData> refIncomingEdge) {
    TreeMap<TurnDirection, List<Edge<JunctionData, WayData>>> edgesByTurnAggregator = angleRangesWithTurns.stream()
        .collect(Collectors.toMap(
            Pair::getRight,
            pair -> new ArrayList<>(),
            (edges1, edges2) -> new ArrayList<>(),
            TreeMap::new
        ));

    outgoingEdges.stream()
        .map(outgoingEdge -> Pair.of(outgoingEdge, Vector.calculateAngleBetween(
            new Vector(
                Point.convertFromCoords(refIncomingEdge.getTarget().getData()),
                Point.convertFromCoords(refIncomingEdge.getSource().getData())),
            new Vector(
                Point.convertFromCoords(outgoingEdge.getSource().getData()),
                Point.convertFromCoords(outgoingEdge.getTarget().getData()))
        )))
        .map(pair -> Pair.of(pair.getLeft(), getAllTurnsFromAngle(pair.getRight())))
        .forEach(pair -> pair.getRight()
            .forEach(turn -> edgesByTurnAggregator.get(turn).add(pair.getLeft())));

    return edgesByTurnAggregator.entrySet().stream()
        .filter(entry -> !entry.getValue().isEmpty())
        .map(entry -> Map.entry(entry.getKey(), entry.getValue().get(entry.getValue().size() / 2)))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
        ));
  }

  private List<TurnDirection> getAllTurnsFromAngle(double angle) {
    return angleRangesWithTurns.stream()
        .filter(entry -> entry.getLeft().getLeft() <= angle && angle <= entry.getLeft().getRight())
        .map(Pair::getRight)
        .collect(Collectors.toList());
  }
}
