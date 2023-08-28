package pl.edu.agh.hiputs.loadbalancer.utils;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

@UtilityClass
public class CarCounterUtil {

    public static int countCars(PatchReader patchReader){
      return patchReader.streamLanesReadable()
          .map(LaneReadable::numberOfCars)
          .reduce(0, Integer::sum);
    }

    public static int countCars(List<PatchId> patches, TransferDataHandler transferDataHandler){
      return patches.stream()
          .map(transferDataHandler::getPatchById)
          .map(CarCounterUtil::countCars).reduce(0, Integer::sum);
    }

  public static int countAllCars(TransferDataHandler transferDataHandler) {
    return transferDataHandler.getKnownPatchReadable()
        .stream()
        .filter(p -> transferDataHandler.isLocalPatch(p.getPatchId()))
        .map(CarCounterUtil::countCars)
        .reduce(0, Integer::sum);
  }

  /**
   * @param patchReader
   *
   * @return Triple - (number of cars, stopped cars, cars summary speed)
   */
  public Triple<Integer, Integer, Double> countCarsStats(PatchReader patchReader) {
    return patchReader.streamLanesReadable()
        .map(lane -> new ImmutableTriple<>(lane.numberOfCars(), lane.getStoppedCars(), lane.getSumSpeed()))
        .reduce(new ImmutableTriple<>(0, 0, 0.0),
            (a, b) -> new ImmutableTriple<>(a.getLeft() + b.getLeft(), a.getMiddle() + b.getMiddle(),
                a.getRight() + b.getRight()));
  }

  public Triple<Integer, Integer, Double> countAllCarStats(TransferDataHandler transferDataHandler) {
    return transferDataHandler.getKnownPatchReadable()
        .stream()
        .filter(p -> transferDataHandler.isLocalPatch(p.getPatchId()))
        .map(CarCounterUtil::countCarsStats)
        .reduce(new ImmutableTriple<>(0, 0, 0.0),
            (a, b) -> new ImmutableTriple<>(a.getLeft() + b.getLeft(), a.getMiddle() + b.getMiddle(),
                a.getRight() + b.getRight()));
  }

}
