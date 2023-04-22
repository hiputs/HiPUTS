package pl.edu.agh.hiputs.model.map.patch;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.Road;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

@Builder
@AllArgsConstructor
public class Patch implements PatchReader, PatchEditor {

  /**
   * Identifier of this Patch
   */
  @Getter
  @Builder.Default
  private final PatchId patchId = PatchId.random();

  /**
   * Junctions within this patch
   */
  private final Map<JunctionId, Junction> junctions;

  /**
   * Roads within this patch
   */
  private final Map<RoadId, Road> roads;

  /**
   * Patches that are adjacent/neighbours to this patch
   */
  @Getter
  @Builder.Default
  private final Set<PatchId> neighboringPatches = new HashSet<>();

  @Override
  public Set<RoadId> getRoadIds() {
    return roads.keySet();
  }

  @Override
  public RoadReadable getRoadReadable(RoadId roadId) {
    return roads.get(roadId);
  }

  @Override
  public RoadEditable getRoadEditable(RoadId roadId) {
    return roads.get(roadId);
  }

  @Override
  public Stream<RoadReadable> streamRoadReadable() {
    return roads.values().stream().map(Function.identity());
  }

  @Override
  public Stream<RoadEditable> streamRoadsEditable() {
    return roads.values().stream().map(Function.identity());
  }

  @Override
  public Stream<RoadEditable> parallelStreamRoadsEditable() {
    return roads.values().parallelStream().map(Function.identity());
  }

  @Override
  public Set<JunctionId> getJunctionIds() {
    return junctions.keySet();
  }

  @Override
  public JunctionReadable getJunctionReadable(JunctionId junctionId) {
    return junctions.get(junctionId);
  }

  @Override
  public JunctionEditable getJunctionEditable(JunctionId junctionId) {
    return junctions.get(junctionId);
  }

  @Override
  public Stream<JunctionReadable> streamJunctionsReadable() {
    return junctions.values().stream().map(Function.identity());
  }

  @Override
  public Stream<JunctionEditable> streamJunctionsEditable() {
    return junctions.values().stream().map(Function.identity());
  }

  @Override
  public RoadEditable getAnyRoad() {
    return roads.values().iterator().next();
  }
}
