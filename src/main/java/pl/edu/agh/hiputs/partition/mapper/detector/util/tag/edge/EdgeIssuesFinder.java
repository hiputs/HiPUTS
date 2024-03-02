package pl.edu.agh.hiputs.partition.mapper.detector.util.tag.edge;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface EdgeIssuesFinder {

  Pair<String, List<Edge<JunctionData, WayData>>> lookup(Graph<JunctionData, WayData> graph);

}
