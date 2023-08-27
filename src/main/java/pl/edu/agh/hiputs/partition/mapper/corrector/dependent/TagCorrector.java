package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.extractor.EdgeExtractor;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.extractor.StandardEdgeExtractor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@RequiredArgsConstructor
public class TagCorrector implements Corrector {
  private final static String knotsSuffix = "knots";
  private final static String mphSuffix = "mph";
  private final static double knotsToKmhMultiplier = 1.852;
  private final static double mphToKmhMultiplier = 1.609344;
  private final EdgeExtractor extractor = new StandardEdgeExtractor();
  private final List<Pair<String, List<Edge<JunctionData, WayData>>>> edgesToFix;
  private final List<Pair<String, List<Node<JunctionData, WayData>>>> nodesToFix;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    edgesToFix.forEach(pair -> fixEdges(pair.getLeft(), pair.getRight()));
    nodesToFix.forEach(pair -> fixNodes(pair.getLeft(), pair.getRight()));

    return graph;
  }

  private void fixEdges(String tag, List<Edge<JunctionData, WayData>> edges) {
    edges.forEach(edge -> {
      Optional<Edge<JunctionData, WayData>> predecessor = extractor.getPredecessorWithKey(edge, tag);
      Optional<Edge<JunctionData, WayData>> successor = extractor.getSuccessorWithKey(edge, tag);

      if (tag.equals("maxspeed")) {
        if (predecessor.isPresent() && successor.isPresent()) {
          edge.getData().getTags().put(tag,
              avgSpeed(predecessor.get().getData().getTags().get(tag), successor.get().getData().getTags().get(tag)));
        } else {
          predecessor.ifPresent(candidate ->
              edge.getData().getTags().put(tag, candidate.getData().getTags().get(tag)));

          successor.ifPresent(candidate ->
              edge.getData().getTags().put(tag, candidate.getData().getTags().get(tag)));
        }
      } else {
        predecessor.ifPresent(candidate ->
            edge.getData().getTags().put(tag, candidate.getData().getTags().get(tag)));

        successor.ifPresent(candidate ->
            edge.getData().getTags().put(tag, candidate.getData().getTags().get(tag)));
      }
    });
  }

  private String avgSpeed(String speed1, String speed2) {
    try {
      return String.format("%d", (validateSpeedString(speed1) + validateSpeedString(speed2)) / 2);
    } catch (Exception e) {
      return new Random().nextBoolean() ? speed1 : speed2;
    }
  }

  private int validateSpeedString(String speed) {
    if (speed.endsWith(mphSuffix)) {
      return (int) (Integer.parseInt(speed.replace(mphSuffix, "").trim()) * mphToKmhMultiplier);
    } else if (speed.endsWith(knotsSuffix)) {
      return (int) (Integer.parseInt(speed.replace(knotsSuffix, "").trim()) * knotsToKmhMultiplier);
    } else {
      return Integer.parseInt(speed);
    }
  }

  private void fixNodes(String tag, List<Node<JunctionData, WayData>> nodes) {
    if (tag.equals("traffic_signals")) {
      nodes.forEach(node -> node.getData().getTags().entrySet().stream()
          .filter(entry -> entry.getValue().equals(tag))
          .findAny()
          .map(Entry::getKey)
          .ifPresent(key ->  node.getData().getTags().remove(key)));
    }
  }
}
