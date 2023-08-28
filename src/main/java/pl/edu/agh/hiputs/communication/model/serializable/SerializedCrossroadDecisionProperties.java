package pl.edu.agh.hiputs.communication.model.serializable;

import java.util.Optional;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CrossroadDecisionProperties;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;

@NoArgsConstructor
public class SerializedCrossroadDecisionProperties implements CustomSerializable<Optional<CrossroadDecisionProperties>> {

  /**
   * Serialized information is properties empty
   */
  private boolean isEmpty;

  /**
   * Serialized blockingCarId on crossroad
   */
  private String blockingCarId;

  /**
   * Serialized lockStepsCount
   */
  private int lockStepsCount;

  /**
   * Serialized complianceFactor - random generated with first CrossroadDecisionProperties
   */
  private int complianceFactor;

  /**
   * Serialized isAvailableSpaceAfterCrossroad
   */
  private boolean isAvailableSpaceAfterCrossroad;

  /**
   * Serialized movePermanentRoadId empty if no value
   */
  private String movePermanentRoadId;

  /**
   * Serialized movePermanentLaneId empty if no value
   */
  private String movePermanentLaneId;

  /**
   * Serialized giveWayVehicleId empty if no value
   */
  private String giveWayVehicleId;

  public SerializedCrossroadDecisionProperties(Optional<CrossroadDecisionProperties> realObject) {
    if(realObject.isEmpty()){
      isEmpty = true;
      blockingCarId = "";
      lockStepsCount = 0;
      complianceFactor = 0;
      isAvailableSpaceAfterCrossroad = true;
      movePermanentRoadId = "";
      movePermanentLaneId = "";
      giveWayVehicleId = "";
    } else{
      isEmpty = false;
      blockingCarId = realObject.get().getBlockingCarId().getValue();
      lockStepsCount = realObject.get().getLockStepsCount();
      complianceFactor = realObject.get().getComplianceFactor();
      isAvailableSpaceAfterCrossroad = realObject.get().getIsAvailableSpaceAfterCrossroad();
      movePermanentRoadId = realObject.get().getMovePermanentRoadId().isEmpty() ? "" : realObject.get().getMovePermanentRoadId().get().getValue();
      movePermanentLaneId = realObject.get().getMovePermanentLaneId().isEmpty() ? "" : realObject.get().getMovePermanentLaneId().get().getValue();
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
      Optional<LaneId> movePermanentLaneIdOptional = movePermanentLaneId.isEmpty() ? Optional.empty() : Optional.of(new LaneId(
          movePermanentLaneId));
      Optional<CarId> giveWayVehicleIdOptional = giveWayVehicleId.isEmpty() ? Optional.empty() : Optional.of(new CarId(giveWayVehicleId));
      return Optional.of(CrossroadDecisionProperties.builder()
          .blockingCarId(new CarId(blockingCarId))
          .lockStepsCount(lockStepsCount)
          .complianceFactor(complianceFactor)
          .isAvailableSpaceAfterCrossroad(isAvailableSpaceAfterCrossroad)
          .movePermanentRoadId(movePermanentRoadIdOptional)
          .movePermanentLaneId(movePermanentLaneIdOptional)
          .giveWayVehicleId(giveWayVehicleIdOptional)
          .build());
    }
  }
}
