package pl.edu.agh.hiputs.partition.mapper.helper.service.edge.reflector;

import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
public class StandardEdgeReflector implements EdgeReflector {

  @Override
  public Edge<JunctionData, WayData> reverseEdge(Edge<JunctionData, WayData> edge) {
    WayData reversedData = WayData.builder()
        .isOneWay(true)
        .tagsInOppositeMeaning(!edge.getData().isTagsInOppositeMeaning())
        .tags(edge.getData().getTags())
        .length(edge.getData().getLength())
        .maxSpeed(edge.getData().getMaxSpeed())
        .isPriorityRoad(edge.getData().isPriorityRoad())
        .patchId(edge.getData().getPatchId())
        .build();

    Edge<JunctionData, WayData> reversedEdge =
        new Edge<>(edge.getTarget().getId() + "->" + edge.getSource().getId(), reversedData);
    reversedEdge.setSource(edge.getTarget());
    reversedEdge.setTarget(edge.getSource());

    return reversedEdge;
  }
}
