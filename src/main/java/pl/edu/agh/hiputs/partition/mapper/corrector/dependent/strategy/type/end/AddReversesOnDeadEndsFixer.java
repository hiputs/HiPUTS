package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end;

import java.util.List;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
public class AddReversesOnDeadEndsFixer implements DeadEndsFixer{

  @Override
  public Graph<JunctionData, WayData> fixFoundDeadEnds(List<DeadEnd> deadEnds, Graph<JunctionData, WayData> graph) {
    deadEnds.forEach(deadEnd ->
        deadEnd.getConnectingEdges().forEach(connectingEdge ->
            graph.addEdge(createReversedEdge(connectingEdge))));

    return graph;
  }

  private Edge<JunctionData, WayData> createReversedEdge(Edge<JunctionData, WayData> edge) {
    WayData reversedData = WayData.builder()
        .isOneWay(true)
        .tagsInOppositeMeaning(!edge.getData().isTagsInOppositeMeaning())
        .tags(edge.getData().getTags())
        .length(edge.getData().getLength())
        .maxSpeed(edge.getData().getMaxSpeed())
        .isPriorityRoad(edge.getData().isPriorityRoad())
        .patchId(edge.getData().getPatchId())
        .build();

    Edge<JunctionData, WayData> reversedEdge = new Edge<>(
        edge.getTarget().getId() + "->" + edge.getSource().getId(), reversedData);
    reversedEdge.setSource(edge.getTarget());
    reversedEdge.setTarget(edge.getSource());

    return reversedEdge;
  }
}
