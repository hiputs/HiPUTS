package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.geom.Point;
import pl.edu.agh.hiputs.partition.model.geom.Vector;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(11)
public class SortedRoadsRequirement implements Requirement{

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream().allMatch(this::areRoadsSortedOnNode);
  }

  @Override
  public String getName() {
    return "11. Roads are sorted on every node.";
  }

  private boolean areRoadsSortedOnNode(Node<JunctionData, WayData> node) {
    return arePointsSorted(Point.convertFromCoords(node.getData()), node.getIncomingEdges().stream()
            .map(edge -> Point.convertFromCoords(edge.getSource().getData()))
            .toList()) &&
        arePointsSorted(Point.convertFromCoords(node.getData()), node.getOutgoingEdges().stream()
            .map(edge -> Point.convertFromCoords(edge.getTarget().getData()))
            .toList());
  }

  private boolean arePointsSorted(Point centerPoint, List<Point> points) {
    if (points.size() <= 1) {
      return true;
    }

    List<Vector> vectors = points.stream()
        .map(point -> new Vector(centerPoint, point))
        .toList();

    Vector mainVector = vectors.get(0);
    List<Double> angles = vectors.stream()
        .skip(1)
        .map(vector -> Vector.calculateAngleBetween(mainVector, vector))
        .toList();

    for(int i = 1; i < angles.size(); i++) {
      if (angles.get(i-1) >= angles.get(i)) {
        return false;
      }
    }

    return true;
  }
}
