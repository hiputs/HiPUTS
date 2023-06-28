package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity;

import java.util.List;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface BridgesCreator {

  Graph<JunctionData, WayData> createBetweenCCsOnGraph(
      List<StronglyConnectedComponent> sCCs,
      List<WeaklyConnectedComponent> wCCs,
      Graph<JunctionData, WayData> graph
  );

}
