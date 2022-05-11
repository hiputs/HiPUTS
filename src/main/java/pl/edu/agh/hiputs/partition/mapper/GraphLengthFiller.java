package pl.edu.agh.hiputs.partition.mapper;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.utils.CoordinatesUtil;

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
