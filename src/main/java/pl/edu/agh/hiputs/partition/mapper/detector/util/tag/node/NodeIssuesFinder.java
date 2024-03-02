package pl.edu.agh.hiputs.partition.mapper.detector.util.tag.node;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public interface NodeIssuesFinder {

  Pair<String, List<Node<JunctionData, WayData>>> lookup(Graph<JunctionData, WayData> graph);

}
