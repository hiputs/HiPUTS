package pl.edu.agh.hiputs.partition.mapper.util.group;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
@Primary
@RequiredArgsConstructor
public class MixedTwoAngleIndexGGRExtractor implements GreenGroupRoadsExtractor{
  private final static double ANGLE_DIFF = 180.0;

  private final AtIndexEdgesListSplitter atIndexEdgesListSplitter;
  private final Angle2EdgeMapCreator angle2EdgeMapCreator;

  @Override
  public List<List<Edge<JunctionData, WayData>>> extract(List<Edge<JunctionData, WayData>> edges) {
    if (edges.size() <= 2) {
      return edges.stream()
          .map(List::of)
          .collect(Collectors.toList());
    }

    TreeMap<Double, Edge<JunctionData, WayData>> angle2EdgeMap = angle2EdgeMapCreator.create(edges, edges.get(0));

    Edge<JunctionData, WayData> oppositeStraightEdge = angle2EdgeMap.entrySet().stream()
        .map(entry -> Map.entry(Math.abs(ANGLE_DIFF - entry.getKey()), entry.getValue()))
        .min(Comparator.comparingDouble(Map.Entry::getKey))
        .map(Map.Entry::getValue)
        .orElse(null);

    int edgesIndex = edges.indexOf(oppositeStraightEdge);
    int splitIndex = oppositeStraightEdge == null || edgesIndex == -1 ? edges.size() / 2 : edgesIndex;

    return atIndexEdgesListSplitter.split(edges, splitIndex);
  }
}
