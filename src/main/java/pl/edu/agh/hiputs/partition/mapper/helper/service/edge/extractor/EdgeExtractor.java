package pl.edu.agh.hiputs.partition.mapper.helper.service.edge.extractor;

import java.util.Optional;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface EdgeExtractor {

  Optional<Edge<JunctionData, WayData>> getPredecessorWithKey(Edge<JunctionData, WayData> edge, String key);

  Optional<Edge<JunctionData, WayData>> getSuccessorWithKey(Edge<JunctionData, WayData> edge, String key);
}
