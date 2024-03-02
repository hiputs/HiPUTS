package pl.edu.agh.hiputs.partition.mapper.detector.util.tag.node;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
public class TrafficSignalsNodeIssuesFinder implements NodeIssuesFinder {

  private final static String key = "highway";
  private final static String value = "traffic_signals";

  /**
   * Find unnecessary annotation traffic_signals from nodes without incoming edges
   *
   * @param graph - whole map
   *
   * @return - pair containing key indicator for corrector/report and list of affected nodes
   */
  @Override
  public Pair<String, List<Node<JunctionData, WayData>>> lookup(Graph<JunctionData, WayData> graph) {
    return Pair.of(value, graph.getNodes()
        .values()
        .stream()
        .filter(node -> node.getData().getTags().containsKey(key) && node.getData().getTags().get(key).equals(value))
        .filter(node -> node.getIncomingEdges().size() < 1)
        .collect(Collectors.toList()));
  }
}
