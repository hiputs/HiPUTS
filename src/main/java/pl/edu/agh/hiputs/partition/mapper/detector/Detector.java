package pl.edu.agh.hiputs.partition.mapper.detector;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface Detector {

  void detect(Graph<JunctionData, WayData> graph);
}
