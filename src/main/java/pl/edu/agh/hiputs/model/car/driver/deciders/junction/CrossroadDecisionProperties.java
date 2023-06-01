package pl.edu.agh.hiputs.model.car.driver.deciders.junction;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;

@AllArgsConstructor
@Getter
@Builder
@ToString
public class CrossroadDecisionProperties {
  private CarId blockingCarId;
  private int lockStepsCount;
  private int complianceFactor;
  private boolean isAvailableSpaceAfterCrossroad;
  private Optional<RoadId> movePermanentRoadId;
  private Optional<LaneId> movePermanentLaneId;
  private Optional<CarId> giveWayVehicleId;

  public boolean getIsAvailableSpaceAfterCrossroad() {
    return isAvailableSpaceAfterCrossroad;
  }
}
