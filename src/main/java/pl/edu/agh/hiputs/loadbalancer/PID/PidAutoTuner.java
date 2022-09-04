package pl.edu.agh.hiputs.loadbalancer.PID;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface PidAutoTuner {

  PIDParameters getParameters();

  void nextValue(double signal, int iteration);

  void setCycles(int cycles);

  boolean isFinished();

  void setTarget(double target);

  void reset(int cycles, double target, int iteration);

  @Getter
  @AllArgsConstructor
  class PIDParameters {
    private final double kP;
    private final double kI;
    private final double kD;
  }

}
