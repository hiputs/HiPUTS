package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.component;

import java.util.List;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface AtIndexEdgesListSplitter {

  List<List<Edge<JunctionData, WayData>>> split(List<Edge<JunctionData, WayData>> edges, int index);
}
