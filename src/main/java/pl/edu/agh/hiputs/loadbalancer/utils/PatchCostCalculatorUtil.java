package pl.edu.agh.hiputs.loadbalancer.utils;

import lombok.experimental.UtilityClass;
import pl.edu.agh.hiputs.loadbalancer.model.PatchBalancingInfo;

@UtilityClass
public class PatchCostCalculatorUtil {

  private static final float PATCHES_DIFF = 5;
  private static final float PART_A_WEIGHT = 0.50f;
  private static final float PART_B_WEIGHT = 0.25f;
  private static final float PART_C_WEIGHT = 0.25f;
  /**
   * Calculate patch cost by parts. Parts are between 0 - 1
   */
  public static double calculateCost(PatchBalancingInfo info, long carBalanceTarget){

    double partA = getMyCarsRatio(info, carBalanceTarget);
    double partB = getContactCarRatio(info, carBalanceTarget);
    double partC = getConcatPatchesRatio(info);

    return partA * PART_A_WEIGHT +
        partB * PART_B_WEIGHT +
        partC * PART_C_WEIGHT;
  }

  private static double getConcatPatchesRatio(PatchBalancingInfo info) {
    return flatValue((
        info.getNewBorderPatchesAfterTransfer().size() -
        info.getShadowPatchesToRemoveAfterTransfer().size()) / PATCHES_DIFF);
  }

  private static double getContactCarRatio(PatchBalancingInfo info, float carBalanceTarget) {
    double diffCar = info.getCountCarsInNewBorderPatches() - info.getCountCarsInRemovedShadowPatches();
    double ratio =  diffCar / 10d / carBalanceTarget;

    return flatValue(ratio);
  }

  private static double flatValue(double ratio) {
    if (ratio > 0.5d){
      return 1d;
    } else if (ratio < -0.5d ){
      return 0f;
    }

    return ratio + 0.5d;
  }

  private static double getMyCarsRatio(PatchBalancingInfo info, long carBalanceTarget) {
    return Math.min(
        Math.abs(carBalanceTarget - info.getCountOfVehicle()) / carBalanceTarget,
        1
    );
  }

}
