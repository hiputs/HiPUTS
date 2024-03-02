package pl.edu.agh.hiputs.partition.mapper.helper.service.crossroad;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public interface CrossroadDeterminer {

  boolean determine(Node<JunctionData, WayData> node);
}
