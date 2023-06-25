package pl.edu.agh.hiputs.partition.mapper.detector.util.tag.edge;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.EdgeExtractor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(2)
@RequiredArgsConstructor
public class CountryEdgeIssuesFinder implements EdgeIssuesFinder{
  private final static String key = "addr:country";
  private final EdgeExtractor extractor;

  /**
   * Detect whether address country changes or is not present between edges from the same country
   * @param graph - whole map
   * @return - pair containing key indicator for corrector/report and list of affected edges
   */
  @Override
  public Pair<String, List<Edge<JunctionData, WayData>>> lookup(Graph<JunctionData, WayData> graph) {
    return Pair.of(key, graph.getEdges().values().stream()
        .filter(this::isBetweenBends)
        .filter(this::differsWithPredecessorAndSuccessor)
        .collect(Collectors.toList()));
  }

  private boolean isBetweenBends(Edge<JunctionData, WayData> edge) {
    return !edge.getSource().getData().isCrossroad() && !edge.getTarget().getData().isCrossroad();
  }

  private boolean differsWithPredecessorAndSuccessor(Edge<JunctionData, WayData> edge) {
    Optional<Edge<JunctionData, WayData>> predecessor = extractor.getPredecessorWithKey(edge, key);
    Optional<Edge<JunctionData, WayData>> successor = extractor.getSuccessorWithKey(edge, key);

    if (predecessor.isPresent() && successor.isPresent()) {
      Map<String, String> predecessorTags = predecessor.get().getData().getTags();
      Map<String, String> successorTags = successor.get().getData().getTags();
      Map<String, String> edgeTags = edge.getData().getTags();

      return predecessorTags.get(key).equals(successorTags.get(key)) &&
          (!edgeTags.containsKey(key) || !predecessorTags.get(key).equals(edgeTags.get(key)));
    }

    return false;
  }
}
