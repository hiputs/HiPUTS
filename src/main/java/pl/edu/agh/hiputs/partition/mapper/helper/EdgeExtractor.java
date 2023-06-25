package pl.edu.agh.hiputs.partition.mapper.helper;

import java.util.Optional;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
public class EdgeExtractor {

  public Optional<Edge<JunctionData, WayData>> getPredecessorWithKey(Edge<JunctionData, WayData> edge, String key) {
    return edge.getSource().getIncomingEdges().stream()
        .filter(candidate -> candidate.getData().getTags().containsKey(key))
        .filter(candidate -> !edge.getTarget().getOutgoingEdges().contains(candidate))
        .findAny();
  }

  public Optional<Edge<JunctionData, WayData>> getSuccessorWithKey(Edge<JunctionData, WayData> edge, String key) {
    return edge.getTarget().getOutgoingEdges().stream()
        .filter(candidate -> candidate.getData().getTags().containsKey(key))
        .filter(candidate -> !edge.getSource().getIncomingEdges().contains(candidate))
        .findAny();
  }
}
