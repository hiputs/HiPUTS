package pl.edu.agh.hiputs.partition.mapper;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.HorizontalSign;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction.JunctionBuilder;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.model.map.roadstructure.NeighborLaneInfo;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.visualization.web.Coordinates;

@Service
public class Internal2SimulationModelMapperImpl implements Internal2SimulationModelMapper {

  public Map<PatchId, Patch> mapToSimulationModel(Graph<PatchData, PatchConnectionData> graph) {
    return graph.getNodes().values().parallelStream()
        .map(this::mapPatchToSimulationModel)
        .collect(Collectors.toMap(Patch::getPatchId, Function.identity()));
  }

  private Patch mapPatchToSimulationModel(Node<PatchData, PatchConnectionData> patch) {

    Set<PatchId> neighbouringPatches = new HashSet<>();

    neighbouringPatches.addAll(patch.getOutgoingEdges()
        .parallelStream()
        .map(e -> new PatchId(e.getTarget().getId()))
        .toList());

    neighbouringPatches.addAll(patch.getIncomingEdges()
        .parallelStream()
        .map(e -> new PatchId(e.getTarget().getId()))
        .toList());

    PatchId id = new PatchId(patch.getId());
    neighbouringPatches.remove(id);


    return Patch.builder()
        .patchId(id)
        .lanes(edgesParallelStream(patch).map(this::mapEdgeToSimulationModel)
            .collect(Collectors.toMap(Lane::getLaneId, Function.identity())))
        .junctions(nodesParallelStream(patch).map(this::mapNodeToSimulationModel)
            .collect(Collectors.toMap(Junction::getJunctionId, Function.identity())))
        .neighboringPatches(neighbouringPatches)
        .build();
  }

  private Stream<Edge<JunctionData, WayData>> edgesParallelStream(Node<PatchData, PatchConnectionData> patch) {
    return patch.getData().getGraphInsidePatch().getEdges().values().parallelStream();
  }

  private Stream<Node<JunctionData, WayData>> nodesParallelStream(Node<PatchData, PatchConnectionData> patch) {
    return patch.getData().getGraphInsidePatch().getNodes().values().parallelStream();
  }

  private Junction mapNodeToSimulationModel(Node<JunctionData, WayData> node) {
    JunctionBuilder junctionBuilder = Junction.builder()
        .junctionId(new JunctionId(node.getId(), getJunctionType(node)))
        .longitude(node.getData().getLon())
        .latitude(node.getData().getLat());

    node.getIncomingEdges()
        .forEach(e -> junctionBuilder.addIncomingLaneId(new LaneId(e.getId()), !e.getData().isPriorityRoad()));

    node.getOutgoingEdges().forEach(e -> junctionBuilder.addOutgoingLaneId(new LaneId(e.getId())));

    return junctionBuilder.build();
  }

  private Lane mapEdgeToSimulationModel(Edge<JunctionData, WayData> edge) {
    Lane.LaneBuilder laneBuilder = Lane.builder()
        .laneId(new LaneId(edge.getId()))
        .length(edge.getData().getLength())
        .incomingJunctionId(new JunctionId(edge.getSource().getId(), getJunctionType(edge.getSource())))
        .outgoingJunctionId(new JunctionId(edge.getTarget().getId(), getJunctionType(edge.getTarget())))
        .start(new Coordinates(edge.getSource().getData().getLat(),edge.getSource().getData().getLon()))
        .end(new Coordinates(edge.getTarget().getData().getLat(),edge.getTarget().getData().getLon()))
        .leftNeighbor(getOppositeLaneId(edge).map(laneId -> new NeighborLaneInfo(laneId,
            HorizontalSign.OPPOSITE_DIRECTION_DOTTED_LINE))); //todo parse line from osm if possible
    return laneBuilder.build();
  }

  private JunctionType getJunctionType(Node<JunctionData, WayData> node) {
    if (node.getData() == null ) return null; // todo this line should be removed
    return node.getData().isCrossroad() ? JunctionType.CROSSROAD : JunctionType.BEND;
  }

  private Optional<LaneId> getOppositeLaneId(Edge<JunctionData, WayData> edge) {
    if (edge.getData().isOneWay()) {
      return Optional.empty();
    }

    return edge.getSource()
        .getIncomingEdges()
        .stream()
        .filter(e -> e.getSource().equals(edge.getTarget()))
        .map(e -> new LaneId(e.getId()))
        .findFirst();
  }
}
