package pl.edu.agh.hiputs.partition.mapper.detector.util.tag.edge;

import java.util.List;
import java.util.Map;
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
@Order(1)
@RequiredArgsConstructor
public class NameEdgeIssuesFinder implements EdgeIssuesFinder {

  private final static String key = "name";
  private final EdgeExtractor extractor;

  /**
   * Detect whether there is unnamed road after & before road already containing name
   * or these names are non-equal and there is no crossroad between these edges
   *
   * @param graph - whole map
   *
   * @return - pair containing key indicator for corrector/report and list of affected edges
   */
  @Override
  public Pair<String, List<Edge<JunctionData, WayData>>> lookup(Graph<JunctionData, WayData> graph) {
    return Pair.of(key, graph.getEdges().values().stream().filter(this::inequality).collect(Collectors.toList()));
  }

  private boolean inequality(Edge<JunctionData, WayData> edge) {
    return inequalityWithPredecessor(edge) && inequalityWithSuccessor(edge);
  }

  private boolean inequalityWithPredecessor(Edge<JunctionData, WayData> edge) {
    if (edge.getSource().getData().isCrossroad()) {
      return false;
    }

    return extractor.getPredecessorWithKey(edge, key)
        .map(predecessor -> notEqualNamesInEdges(edge, predecessor))
        .orElse(false);
  }

  private boolean inequalityWithSuccessor(Edge<JunctionData, WayData> edge) {
    if (edge.getTarget().getData().isCrossroad()) {
      return false;
    }

    return extractor.getSuccessorWithKey(edge, key)
        .map(successor -> notEqualNamesInEdges(edge, successor))
        .orElse(false);
  }

  private boolean notEqualNamesInEdges(Edge<JunctionData, WayData> current, Edge<JunctionData, WayData> candidate) {
    Map<String, String> currentTags = current.getData().getTags();
    Map<String, String> candidateTags = candidate.getData().getTags();

    if (!currentTags.containsKey(key)) {
      return true;
    }

    return !currentTags.get(key).equals(candidateTags.get(key));
  }
}
