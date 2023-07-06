package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority.component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public abstract class AbstractPriorityProcessor implements PriorityProcessor{

  protected Optional<Edge<JunctionData, WayData>> compareRoadsByValue(
      List<Edge<JunctionData, WayData>> edges, Function<Edge<JunctionData, WayData>, Integer> valueRetriever) {
    if (edges.isEmpty()) {
      return Optional.empty();
    }

    List<Edge<JunctionData, WayData>> sortedEdges = edges.stream()
        .sorted(Collections.reverseOrder(Comparator.comparingInt(valueRetriever::apply)))
        .toList();

    if (valueRetriever.apply(sortedEdges.get(0)).equals(valueRetriever.apply(sortedEdges.get(sortedEdges.size() - 1)))) {
      return Optional.empty();  // delegation
    }

    return Optional.of(sortedEdges.get(0));
  }
}
