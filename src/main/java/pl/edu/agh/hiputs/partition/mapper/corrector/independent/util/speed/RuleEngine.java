package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public interface RuleEngine {

  Integer processWay(Edge<JunctionData, WayData> edge);
}
