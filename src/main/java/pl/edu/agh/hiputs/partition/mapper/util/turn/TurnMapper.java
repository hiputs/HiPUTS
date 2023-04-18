package pl.edu.agh.hiputs.partition.mapper.util.turn;

import java.util.List;
import java.util.Map;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface TurnMapper {

  Map<TurnDirection, Edge<JunctionData, WayData>> assignTurns2OutgoingEdges(
      List<Edge<JunctionData, WayData>> outgoingEdges,
      Edge<JunctionData, WayData> refIncomingEdge
  );
}
