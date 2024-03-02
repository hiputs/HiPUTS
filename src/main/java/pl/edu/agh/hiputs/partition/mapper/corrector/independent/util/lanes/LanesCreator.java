package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.lanes;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface LanesCreator {

  void deduceAndCreate(Edge<JunctionData, WayData> edge);
}
