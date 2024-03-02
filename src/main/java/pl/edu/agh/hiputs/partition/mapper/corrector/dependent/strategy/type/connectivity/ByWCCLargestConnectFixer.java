package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;

@Service
public class ByWCCLargestConnectFixer implements ConnectFixer {

  @Override
  public Graph<JunctionData, WayData> fixFoundDisconnections(List<StronglyConnectedComponent> sCCs,
      List<WeaklyConnectedComponent> wCCs, Graph<JunctionData, WayData> graph) {
    return wCCs.stream()
        .max(Comparator.comparingInt(wCC -> wCC.getNodesIds().size()))
        .map(WeaklyConnectedComponent::getNodesIds)
        .map(nodesIdSet -> createGraphFromWCC(nodesIdSet, graph))
        .orElse(graph);
  }

  private Graph<JunctionData, WayData> createGraphFromWCC(Set<String> nodesIds, Graph<JunctionData, WayData> graph) {
    GraphBuilder<JunctionData, WayData> newGraphBuilder = new GraphBuilder<>();

    nodesIds.stream().map(nodeId -> graph.getNodes().get(nodeId)).forEach(newGraphBuilder::addNode);

    graph.getEdges()
        .values()
        .stream()
        .filter(edge -> nodesIds.contains(edge.getSource().getId()) && nodesIds.contains(edge.getTarget().getId()))
        .forEach(newGraphBuilder::addEdge);

    return newGraphBuilder.build();
  }
}
