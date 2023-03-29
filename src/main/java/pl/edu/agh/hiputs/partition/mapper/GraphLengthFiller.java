package pl.edu.agh.hiputs.partition.mapper;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;

@Service
@Order(3)
class GraphLengthFiller implements GraphTransformer {

  @Override
  public Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph) {
    for (Edge<JunctionData, WayData> edge : graph.getEdges().values()) {
      double length = calculateLength(edge);
      edge.getData().setLength(length);
    }
    return graph;
  }

  private double calculateLength(Edge<JunctionData, WayData> edge) {
    return CoordinatesUtil.plainDistanceInMeters(edge.getSource().getData().getLat(), edge.getTarget().getData().getLat(),
        edge.getSource().getData().getLon(), edge.getTarget().getData().getLon());
  }

}
