package pl.edu.agh.hiputs.partition.service.bfs;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public class EuclideanDistance implements Measure<Edge<JunctionData, WayData>> {

  @Override
  public double measure(Edge<JunctionData, WayData> lane) {
    return lane.getData().getLength();
  }

}
