package pl.edu.agh.hiputs.partition.mapper.detector.util.tag.edge;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.extractor.EdgeExtractor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(3)
@RequiredArgsConstructor
public class MaxSpeedEdgeIssuesFinder implements EdgeIssuesFinder {

  private final static String key = "maxspeed";
  private final EdgeExtractor extractor;

  /**
   * Detect lack of max speed between roads with max speed already defined
   *
   * @param graph - whole map
   *
   * @return - pair containing key indicator for corrector/report and list of affected edges
   */
  @Override
  public Pair<String, List<Edge<JunctionData, WayData>>> lookup(Graph<JunctionData, WayData> graph) {
    return Pair.of(key, graph.getEdges()
        .values()
        .stream()
        .filter(this::isBetweenBends)
        .filter(this::lacksBetweenDefinedPredecessorAndSuccessor)
        .collect(Collectors.toList()));
  }

  private boolean isBetweenBends(Edge<JunctionData, WayData> edge) {
    return !edge.getSource().getData().isCrossroad() && !edge.getTarget().getData().isCrossroad();
  }

  private boolean lacksBetweenDefinedPredecessorAndSuccessor(Edge<JunctionData, WayData> edge) {
    Optional<Edge<JunctionData, WayData>> predecessor = extractor.getPredecessorWithKey(edge, key);
    Optional<Edge<JunctionData, WayData>> successor = extractor.getSuccessorWithKey(edge, key);

    return predecessor.isPresent() && successor.isPresent() && !edge.getData().getTags().containsKey(key);
  }
}
