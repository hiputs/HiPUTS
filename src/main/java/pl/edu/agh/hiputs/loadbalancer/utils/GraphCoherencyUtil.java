package pl.edu.agh.hiputs.loadbalancer.utils;

import lombok.experimental.UtilityClass;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

@UtilityClass
public class GraphCoherencyUtil {

    public static boolean isCoherency(TransferDataHandler transferDataHandler, PatchId removedPatchId){
      return true;
    }
}
