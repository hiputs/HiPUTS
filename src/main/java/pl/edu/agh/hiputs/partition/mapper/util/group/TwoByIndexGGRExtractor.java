package pl.edu.agh.hiputs.partition.mapper.util.group;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
public class TwoByIndexGGRExtractor implements GreenGroupRoadsExtractor, AtIndexEdgesListSplitter{

  @Override
  public List<List<Edge<JunctionData, WayData>>> extract(List<Edge<JunctionData, WayData>> edges) {
    return split(edges, edges.size() / 2);
  }

  @Override
  public List<List<Edge<JunctionData, WayData>>> split(List<Edge<JunctionData, WayData>> edges, int index) {
    List<Edge<JunctionData, WayData>> firstPart = edges.subList(0, index);
    List<Edge<JunctionData, WayData>> secondPart = edges.subList(index, edges.size());

    List<List<Edge<JunctionData, WayData>>> result = IntStream.range(0, Math.min(firstPart.size(), secondPart.size()))
        .mapToObj(i -> List.of(firstPart.get(i), secondPart.get(i)))
        .collect(Collectors.toList());

    if (edges.size() % 2 != 0) {
      result.add(List.of(secondPart.get(secondPart.size() - 1)));
    }

    return result;
  }
}
