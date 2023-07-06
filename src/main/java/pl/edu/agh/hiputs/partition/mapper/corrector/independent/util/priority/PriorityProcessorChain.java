package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority;

import java.util.List;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface PriorityProcessorChain {

  Edge<JunctionData, WayData> getTopPriorityRoad(List<Edge<JunctionData, WayData>> edges);

}
