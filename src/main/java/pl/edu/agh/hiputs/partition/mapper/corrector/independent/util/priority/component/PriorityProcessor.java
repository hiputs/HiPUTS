package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority.component;

import java.util.List;
import java.util.Optional;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface PriorityProcessor {

  Optional<Edge<JunctionData, WayData>> compareRoads(List<Edge<JunctionData, WayData>> edges);

}
