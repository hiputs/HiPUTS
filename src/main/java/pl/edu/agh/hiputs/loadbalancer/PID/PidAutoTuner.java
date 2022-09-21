package pl.edu.agh.hiputs.loadbalancer.PID;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface PidAutoTuner {

  PIDParameters getParameters();

  double nextValue(double signal, int iteration);

  boolean isFinished();

  void setTarget(double target);

  void reset(double target, int iteration, double min, double max);

  @Getter
  @AllArgsConstructor
  class PIDParameters {
    private final double kP;
    private final double kI;
    private final double kD;
  }

}
