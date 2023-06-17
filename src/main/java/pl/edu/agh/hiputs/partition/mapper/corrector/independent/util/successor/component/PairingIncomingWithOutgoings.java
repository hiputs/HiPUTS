package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component;

import java.util.List;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface PairingIncomingWithOutgoings {

  void pair(WayData incomingEdge, List<Edge<JunctionData, WayData>> outgoingEdges);
}
