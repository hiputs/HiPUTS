package pl.edu.agh.hiputs.partition.mapper.util.turn;

import java.util.List;
import java.util.Map;
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
public class FixedAngleRangeTurnMapper implements TurnMapper{
  private final TreeMap<Double, TurnDirection> maxAngle2TurnMap = new TreeMap<>() {{
    put(45.0, TurnDirection.SHARP_LEFT);
    put(120.0, TurnDirection.LEFT);
    put(150.0, TurnDirection.SLIGHT_LEFT);
    put(210.0, TurnDirection.THROUGH);
    put(240.0, TurnDirection.SLIGHT_RIGHT);
    put(315.0, TurnDirection.RIGHT);
    put(360.0, TurnDirection.SHARP_RIGHT);
  }};

  @Override
  public Map<TurnDirection, Edge<JunctionData, WayData>> assignTurns2OutgoingEdges(
      List<Edge<JunctionData, WayData>> outgoingEdges, Edge<JunctionData, WayData> refIncomingEdge) {
    TreeMap<TurnDirection, List<Edge<JunctionData, WayData>>> edgesByTurnAggregator = outgoingEdges.stream()
        .collect(Collectors.toMap(
            outgoingEdge -> mapAngleToTurn(
                Vector.calculateAngleBetween(
                    new Vector(
                        Point.convertFromCoords(refIncomingEdge.getTarget().getData()),
                        Point.convertFromCoords(refIncomingEdge.getSource().getData())),
                    new Vector(
                        Point.convertFromCoords(outgoingEdge.getSource().getData()),
                        Point.convertFromCoords(outgoingEdge.getTarget().getData()))
                )),
            List::of,
            (edges1, edges2) -> Stream.concat(edges1.stream(), edges2.stream()).toList(),
            TreeMap::new
        ));

    return edgesByTurnAggregator.entrySet().stream()
        .map(entry -> Map.entry(entry.getKey(), entry.getValue().get(entry.getValue().size() / 2)))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
        ));
  }

  private TurnDirection mapAngleToTurn(double angle) {
    return maxAngle2TurnMap.ceilingEntry(angle).getValue();
  }
}
