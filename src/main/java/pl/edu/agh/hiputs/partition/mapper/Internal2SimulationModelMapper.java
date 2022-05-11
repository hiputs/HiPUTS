package pl.edu.agh.hiputs.partition.mapper;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction.JunctionBuilder;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class Internal2SimulationModelMapper {

  public MapFragment internal2SimulationModel(Graph<PatchData, PatchConnectionData> graph, Set<String> localPatchesIds) {

    Map<PatchId, Patch> knownPatches = graph.getEdges().values().stream()
        .filter(patchConnection -> localPatchesIds.contains(patchConnection.getSource().getId()) ||
            localPatchesIds.contains(patchConnection.getTarget().getId()))
        .flatMap(patchConnection -> Stream.of(patchConnection.getSource(), patchConnection.getTarget()))
        .map(this::mapPatchToSimulationModel)
        .collect(Collectors.toMap(Patch::getPatchId, Function.identity()));

    Map<PatchId, Patch> localPatches = knownPatches.values().stream()
        .filter(p -> localPatchesIds.contains(p.getPatchId().getValue()))
        .collect(Collectors.toMap(Patch::getPatchId, Function.identity()));

    Map<PatchId, Patch> remotePatches = knownPatches.values().stream()
        .filter(p -> !localPatchesIds.contains(p.getPatchId().getValue()))
        .collect(Collectors.toMap(Patch::getPatchId, Function.identity()));

    return MapFragment.builder()
        .addRemotePatch()
        .addLocalPatch()
        .build();
  }

  private Patch mapPatchToSimulationModel(Node<PatchData, PatchConnectionData> patch) {
    return Patch.builder()
        .patchId(new PatchId(patch.getId()))
        .lanes(patch.getData().getGraphInsidePatch().getEdges().values().stream().map(this::mapEdgeToSimulationModel).collect(
            Collectors.toMap(Lane::getLaneId, Function.identity())))
        .junctions(patch.getData().getGraphInsidePatch().getNodes().values().stream().map(this::mapNodeToSimulationModel).collect(
            Collectors.toMap(Junction::getJunctionId, Function.identity())))
        .neighboringPatches(patch.getOutgoingEdges().stream().map(e -> new PatchId(e.getTarget().getId())).collect(
            Collectors.toSet()))
        .build();
  }

  private Junction mapNodeToSimulationModel(Node<JunctionData, WayData> node) {
    boolean isCrossroad = node.getIncomingEdges().size() > 1;
    JunctionBuilder junctionBuilder = Junction.builder()
        .junctionId(new JunctionId(node.getId(), isCrossroad ? JunctionType.CROSSROAD : JunctionType.BEND));

    node.getIncomingEdges()
        .forEach(e -> junctionBuilder.addIncomingLaneId(new LaneId(e.getId()), !e.getData().isPriorityRoad()));

    node.getOutgoingEdges()
        .forEach(e -> junctionBuilder.addOutgoingLaneId(new LaneId(e.getId())));

    return junctionBuilder.build();
  }

  private Lane mapEdgeToSimulationModel(Edge<JunctionData, WayData> edge) {
    Lane.LaneBuilder laneBuilder = Lane.builder()
        .laneId(new LaneId(edge.getId()))
        .length(edge.getData().getLength())
        .incomingJunctionId(new JunctionId(edge.getSource().getId(), getJunctionType(edge.getSource())))
        .outgoingJunctionId(new JunctionId(edge.getTarget().getId(), getJunctionType(edge.getTarget())))
        .oppositeLaneId(getOppositeLaneId(edge));
        // .outSign()
    return laneBuilder.build();
  }

  private JunctionType getJunctionType(Node<JunctionData, WayData> node) {
    return node.getIncomingEdges().size() > 1 ? JunctionType.CROSSROAD : JunctionType.BEND;
  }

  private Optional<LaneId> getOppositeLaneId(Edge<JunctionData, WayData> edge) {
    if (edge.getData().isOneWay()) {
      return Optional.empty();
    }

    return edge.getSource().getIncomingEdges()
        .stream()
        .filter(e -> e.getSource().equals(edge.getTarget()))
        .map(e -> new LaneId(e.getId()))
        .findFirst();
  }
}
