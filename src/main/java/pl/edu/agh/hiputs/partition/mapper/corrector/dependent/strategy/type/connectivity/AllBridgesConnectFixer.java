package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@RequiredArgsConstructor
public class AllBridgesConnectFixer implements ConnectFixer {

  private final DirectBridgesConnectFixer directedBridgesCreator;
  private final IndirectBridgesConnectFixer undirectedBridgesCreator;

  @Override
  public Graph<JunctionData, WayData> fixFoundDisconnections(List<StronglyConnectedComponent> sCCs,
      List<WeaklyConnectedComponent> wCCs, Graph<JunctionData, WayData> graph) {
    Graph<JunctionData, WayData> newGraph;

    newGraph = directedBridgesCreator.fixFoundDisconnections(sCCs, wCCs, graph);
    newGraph = undirectedBridgesCreator.fixFoundDisconnections(sCCs, wCCs, newGraph);

    return newGraph;
  }
}
