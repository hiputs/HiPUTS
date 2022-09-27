package pl.edu.agh.hiputs.model.car.driver;

import lombok.Value;

@Value
public class DriverParameters {
  private final double IdmDistanceHeadway = 2.0;
  private final double IdmTimeHeadway = 2.0;
  private final double IdmNormalAcceleration = 3.5;
  private final double IdmNormalDeceleration = 2.0;
  private final int IdmDelta = 4;

  private final double TrailTimeDelta = 1.25;
  private final double TrailConflictAreaLength = 5.0;

  private final double DriverTimeStep = 0.5;
  private final int GiveWayWaitTime = 5;
  private final int MovePermanentWaitTime = 40;
}
