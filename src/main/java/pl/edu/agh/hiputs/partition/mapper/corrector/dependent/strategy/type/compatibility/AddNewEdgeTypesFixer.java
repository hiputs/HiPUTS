package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.compatibility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.compatibility.TypeIncompatibility;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
public class AddNewEdgeTypesFixer implements TypesFixer{
  private final static String highwayKey = "highway";

  @Override
  public Graph<JunctionData, WayData> fixFoundTypesIncompatibilities(List<TypeIncompatibility> typeIncompatibilities,
      Graph<JunctionData, WayData> graph) {
    typeIncompatibilities.stream()
        .filter(typeIncompatibility -> graph.getEdges().containsKey(typeIncompatibility.getImpactedEdge().getId()))
        .distinct()
        .forEach(typeIncompatibility -> divideEdgeIntoTwoWithNewNode(typeIncompatibility, graph));

    return graph;
  }

  private void divideEdgeIntoTwoWithNewNode(TypeIncompatibility typeIncompatibility, Graph<JunctionData, WayData> graph) {
    Edge<JunctionData, WayData> foundEdge = typeIncompatibility.getImpactedEdge();

    boolean isPredecessorMotorway = foundEdge.getSource().getIncomingEdges().stream()
        .anyMatch(edge -> isRequiredType(edge, typeIncompatibility.getImpactedType()));
    boolean isSuccessorMotorway = foundEdge.getTarget().getOutgoingEdges().stream()
        .anyMatch(edge -> isRequiredType(edge, typeIncompatibility.getImpactedType()));

    if (isPredecessorMotorway && isSuccessorMotorway) {
      // splitting into 1/3 new road and 2/3 original road
      foundEdge = splitEdgeAndCreateNode(graph, foundEdge, 3, true, typeIncompatibility.getRequiredType());
      // splitting into 1/3 original road and 1/3 new road
      splitEdgeAndCreateNode(graph, foundEdge, 2, false, typeIncompatibility.getRequiredType());
    } else if (isPredecessorMotorway) {
      splitEdgeAndCreateNode(graph, foundEdge, 2, true, typeIncompatibility.getRequiredType());
    } else if (isSuccessorMotorway) {
      splitEdgeAndCreateNode(graph, foundEdge, 2, false, typeIncompatibility.getRequiredType());
    }
  }

  private boolean isRequiredType(Edge<JunctionData, WayData> edge, String type) {
    return edge.getData().getTags().containsKey(highwayKey) &&
        edge.getData().getTags().get(highwayKey).equals(type);
  }

  private Edge<JunctionData, WayData> splitEdgeAndCreateNode(
      Graph<JunctionData, WayData> graph,
      Edge<JunctionData, WayData> currentEdge,
      int splitSizeByPart,
      boolean fromLeft,
      String requiredType
  ) {
    // creating and adding new node to graph in the middle of current edge
    double latDiff = currentEdge.getTarget().getData().getLat() - currentEdge.getSource().getData().getLat();
    double lonDiff = currentEdge.getTarget().getData().getLon() - currentEdge.getSource().getData().getLon();

    JunctionData newNodeData = JunctionData.builder()
        .isCrossroad(false)
        .tags(new HashMap<>())
        .lat(currentEdge.getSource().getData().getLat() + latDiff / splitSizeByPart)
        .lon(currentEdge.getSource().getData().getLon() + lonDiff / splitSizeByPart)
        .build();
    Node<JunctionData, WayData> newNode = new Node<>(UUID.randomUUID().toString(), newNodeData);
    graph.addNode(newNode);

    // creating new way data with correct tags
    WayData newWayData = WayData.builder()
        .isOneWay(currentEdge.getData().isOneWay())
        .tags(fixTypeInTags(currentEdge.getData().getTags(), requiredType))
        .tagsInOppositeMeaning(currentEdge.getData().isTagsInOppositeMeaning())
        .length(currentEdge.getData().getLength() / splitSizeByPart)
        .maxSpeed(currentEdge.getData().getMaxSpeed())
        .isPriorityRoad(currentEdge.getData().isPriorityRoad())
        .patchId(currentEdge.getData().getPatchId())
        .build();

    // creating new edge and updating current edge by changing source/target and id
    Edge<JunctionData, WayData> newEdge;
    Edge<JunctionData, WayData> updatedEdge;
    if (fromLeft) {
      newEdge = new Edge<>(currentEdge.getSource().getId() + "->" + newNode.getId(), newWayData);
      newEdge.setSource(currentEdge.getSource());
      newEdge.setTarget(newNode);

      updatedEdge = new Edge<>(newNode.getId() + "->" + currentEdge.getTarget().getId(), currentEdge.getData());
      updatedEdge.setSource(newNode);
      updatedEdge.setTarget(currentEdge.getTarget());
    } else {
      newEdge = new Edge<>(newNode.getId() + "->" + currentEdge.getTarget().getId(), newWayData);
      newEdge.setSource(newNode);
      newEdge.setTarget(currentEdge.getTarget());

      updatedEdge = new Edge<>(currentEdge.getSource().getId() + "->" + newNode.getId(), currentEdge.getData());
      updatedEdge.setSource(currentEdge.getSource());
      updatedEdge.setTarget(newNode);
      updatedEdge.getData().setLength(currentEdge.getData().getLength() / splitSizeByPart);
    }

    // updating graph
    graph.removeEdgeById(currentEdge.getId());
    graph.addEdge(newEdge);
    graph.addEdge(updatedEdge);

    return updatedEdge;
  }

  private Map<String, String> fixTypeInTags(Map<String, String> oldTags, String type) {
    Map<String, String> newMap = new HashMap<>(oldTags);
    newMap.put(highwayKey, type);

    return newMap;
  }
}
