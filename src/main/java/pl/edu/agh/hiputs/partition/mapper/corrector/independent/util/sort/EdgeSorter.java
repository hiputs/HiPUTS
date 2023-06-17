package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.sort;

import java.util.List;
import java.util.function.Function;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface EdgeSorter {

  List<Edge<JunctionData, WayData>> getSorted(
      List<Edge<JunctionData, WayData>> edgesToSort,
      Edge<JunctionData, WayData> refEdge,
      Function<Edge<JunctionData, WayData>, JunctionData> appropriateNodeGetter,
      Function<Edge<JunctionData, WayData>, JunctionData> centerNodeGetter
  );
}
