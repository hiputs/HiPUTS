package pl.edu.agh.hiputs.partition.mapper.helper.service.edge.reflector;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface EdgeReflector {

  Edge<JunctionData, WayData> reverseEdge(Edge<JunctionData, WayData> edge);

}
