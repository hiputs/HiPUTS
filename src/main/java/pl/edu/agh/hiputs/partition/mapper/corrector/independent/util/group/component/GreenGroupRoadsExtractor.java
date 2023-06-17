package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.component;

import java.util.List;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface GreenGroupRoadsExtractor {

  List<List<Edge<JunctionData, WayData>>> extract(List<Edge<JunctionData, WayData>> edges);
}
