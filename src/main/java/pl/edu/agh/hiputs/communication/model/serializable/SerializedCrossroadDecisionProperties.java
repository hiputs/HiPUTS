package pl.edu.agh.hiputs.communication.model.serializable;

import java.util.Optional;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CrossroadDecisionProperties;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.RoadId;

public class SerializedCrossroadDecisionProperties implements CustomSerializable<Optional<CrossroadDecisionProperties>> {

  /**
   * Serialized information is properties empty
   */
  private final boolean isEmpty;

  /**
   * Serialized blockingCarId on crossroad
   */
  private final String blockingCarId;

  /**
   * Serialized lockStepsCount
   */
  private final int lockStepsCount;

  /**
   * Serialized complianceFactor - random generated with first CrossroadDecisionProperties
   */
  private final int complianceFactor;

  /**
   * Serialized isAvailableSpaceAfterCrossroad
   */
  private final boolean isAvailableSpaceAfterCrossroad;

  /**
   * Serialized movePermanentRoadId empty if no value
   */
  private final String movePermanentRoadId;

  /**
   * Serialized giveWayVehicleId empty if no value
   */
  private final String giveWayVehicleId;

  public SerializedCrossroadDecisionProperties(Optional<CrossroadDecisionProperties> realObject) {
    if(realObject.isEmpty()){
      isEmpty = true;
      blockingCarId = "";
      lockStepsCount = 0;
      complianceFactor = 0;
      isAvailableSpaceAfterCrossroad = true;
      movePermanentRoadId = "";
      giveWayVehicleId = "";
    }
    else{
      isEmpty = false;
      blockingCarId = realObject.get().getBlockingCarId().getValue();
      lockStepsCount = realObject.get().getLockStepsCount();
      complianceFactor = realObject.get().getComplianceFactor();
      isAvailableSpaceAfterCrossroad = realObject.get().getIsAvailableSpaceAfterCrossroad();
      movePermanentRoadId = realObject.get().getMovePermanentRoadId().isEmpty() ? "" : realObject.get().getMovePermanentRoadId().get().getValue();
      giveWayVehicleId = realObject.get().getGiveWayVehicleId().isEmpty() ? "" : realObject.get().getGiveWayVehicleId().get().getValue();
    }
  }

  @Override
  public Optional<CrossroadDecisionProperties> toRealObject() {
    if(isEmpty){
      return Optional.empty();
    }
    else {
      Optional<RoadId> movePermanentRoadIdOptional = movePermanentRoadId.isEmpty() ? Optional.empty() : Optional.of(new RoadId(
          movePermanentRoadId));
      Optional<CarId> giveWayVehicleIdOptional = giveWayVehicleId.isEmpty() ? Optional.empty() : Optional.of(new CarId(giveWayVehicleId));
      return Optional.of(CrossroadDecisionProperties.builder()
          .blockingCarId(new CarId(blockingCarId))
          .lockStepsCount(lockStepsCount)
          .complianceFactor(complianceFactor)
          .isAvailableSpaceAfterCrossroad(isAvailableSpaceAfterCrossroad)
          .movePermanentRoadId(movePermanentRoadIdOptional)
          .giveWayVehicleId(giveWayVehicleIdOptional)
          .build());
    }
  }
}
