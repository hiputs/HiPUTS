package pl.edu.agh.hiputs.loadbalancer.utils;

import java.util.List;
import lombok.experimental.UtilityClass;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

@UtilityClass
public class CarCounterUtil {

    public static int countCars(PatchReader patchReader){
      return patchReader.streamLaneReadable().map(LaneReadable::numberOfCars)
          .reduce(0, Integer::sum);
    }

    public static int countCars(List<PatchId> patches, TransferDataHandler transferDataHandler){
      return patches.stream()
          .map(transferDataHandler::getPatchById)
          .map(CarCounterUtil::countCars)
          .reduce(0, Integer::sum);
    }

    public static int countAllCars(TransferDataHandler transferDataHandler) {
      return transferDataHandler.getKnownPatchReadable()
          .stream()
          .filter(p -> transferDataHandler.isLocalPatch(p.getPatchId()))
          .map(CarCounterUtil::countCars)
          .reduce(0, Integer::sum);
    }
}
