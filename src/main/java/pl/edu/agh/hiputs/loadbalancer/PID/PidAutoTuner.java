package pl.edu.agh.hiputs.loadbalancer.PID;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface PidAutoTuner {

  PIDParameters getParameters();

  void nextValue(double signal, int iteration);

  void setCycles(int cycles);

  boolean isFinished();

  void setTarget(double target);

  void reset(int iteration, double target);

  @Getter
  @AllArgsConstructor
  static class PIDParameters {
    private final double kP = 1.0;
    private final double kI = 1.0;
    private final double kD = 1.0;
  }

}
