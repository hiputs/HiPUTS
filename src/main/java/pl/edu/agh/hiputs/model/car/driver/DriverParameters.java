package pl.edu.agh.hiputs.model.car.driver;

import lombok.Value;
import pl.edu.agh.hiputs.model.Configuration;

@Value
public class DriverParameters {
  double idmDistanceHeadway;
  double idmTimeHeadway;
  double idmNormalAcceleration;
  double idmNormalDeceleration;
  int idmDelta;

  double junctionTimeDeltaFactor;
  double junctionDefaultConflictAreaLength;
  double viewRange;

  double timeStep;
  int giveWayThreshold;
  int movePermanentThreshold;

  public DriverParameters(Configuration configuration) {
    idmDistanceHeadway = configuration.getIdmDistanceHeadway();
    idmTimeHeadway = configuration.getIdmTimeHeadway();
    idmNormalAcceleration = configuration.getMaxAcceleration();
    idmNormalDeceleration = configuration.getMaxDeceleration();
    idmDelta = configuration.getIdmDelta();
    junctionTimeDeltaFactor = configuration.getJunctionSafeTimeDeltaFactor();

    junctionDefaultConflictAreaLength = 5.0;

    viewRange = configuration.getCarViewRange();
    timeStep = configuration.getSimulationTimeStep();
    giveWayThreshold = configuration.getGiveWayThreshold();
    movePermanentThreshold = configuration.getMovePermanentThreshold() >= 0 ? configuration.getMovePermanentThreshold() : Integer.MAX_VALUE;
  }

  public DriverParameters(){
    idmDistanceHeadway = 2.0;
    idmTimeHeadway = 2.0;
    idmNormalAcceleration = 2.0;
    idmNormalDeceleration = 3.5;
    idmDelta = 4;

    junctionTimeDeltaFactor = 1.25;
    junctionDefaultConflictAreaLength = 5.0;
    viewRange = 300.0;

    timeStep = 1;
    giveWayThreshold = 5;
    movePermanentThreshold = 50;
  }
}
