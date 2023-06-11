package pl.edu.agh.hiputs.model.map.patch;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.exception.EntityNotFoundException;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

import static pl.edu.agh.hiputs.utils.CollectionUtil.getOrThrow;

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
   * Lanes within this patch
   */
  public final Map<LaneId, Lane> lanes;

  /**
   * Total length of lanes
   */

  public double getLanesLength(){
    return lanes.values().stream().mapToDouble(Lane::getLength).sum();
  };

  /**
   * Patches that are adjacent/neighbours to this patch
   */
  @Getter
  @Builder.Default
  private final Set<PatchId> neighboringPatches = new HashSet<>();

  public void placeCar(Car car) throws EntityNotFoundException {
    var lane = getOrThrow(lanes, car.getLaneId());
    lane.addNewCar(car);
  }

  @Override
  public Set<LaneId> getLaneIds() {
    return lanes.keySet();
  }

  @Override
  public LaneReadable getLaneReadable(LaneId laneId) {
    return lanes.get(laneId);
  }

  @Override
  public LaneEditable getLaneEditable(LaneId laneId) {
    return lanes.get(laneId);
  }

  @Override
  public Stream<LaneReadable> streamLanesReadable() {
    return lanes.values().stream().map(Function.identity());
  }

  @Override
  public Stream<LaneEditable> streamLanesEditable() {
    return lanes.values().stream().map(Function.identity());
  }

  @Override
  public Stream<LaneEditable> parallelStreamLanesEditable() {
    return lanes.values().parallelStream().map(Function.identity());
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
  public LaneEditable getAnyLane() {
    return lanes.values().iterator().next();
  }
}
