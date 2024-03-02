package pl.edu.agh.hiputs.partition.mapper.util.group;

import java.util.List;
import java.util.TreeMap;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface Angle2EdgeMapCreator {

  TreeMap<Double, Edge<JunctionData, WayData>> create(
      List<Edge<JunctionData, WayData>> edges,
      Edge<JunctionData, WayData> refEdge
  );
}
