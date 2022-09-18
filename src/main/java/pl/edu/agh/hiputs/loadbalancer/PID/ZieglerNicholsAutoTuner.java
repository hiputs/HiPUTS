package pl.edu.agh.hiputs.loadbalancer.PID;

import lombok.Setter;

// Adaptation C/C++ algorithm https://github.com/jackw01/arduino-pid-autotuner
public class ZieglerNicholsAutoTuner implements PidAutoTuner {

  private double target = 0;
  private static final double loopInterval = 1;
  private double minOutput = -300d, maxOutput = 300d;

  @Setter
  private int cycles = 10;

  // See startTuningLoop()
  private int i;
  private boolean output;
  private long t1, t2, tHigh, tLow;
  private double max, min;
  private double pAverage, iAverage, dAverage;

  private double kp, ki, kd;

  private static final double kpConstant = 0.6;
  private static final double tiConstant = 0.5;
  private static final double tdConstant = 0.125;
  private double outputValue = 0d;

  @Override
  public PIDParameters getParameters() {
    return new PIDParameters(kp, ki, kd);
  }

  @Override
  public void setTarget(double target) {
    this.target = target;
  }

  @Override
  public void reset(double target, int iteration, double min, double max) {
    i = 0; // Cycle counter
    output = true; // Current output state
    outputValue = maxOutput;
    t1 = t2 = iteration; // Times used for calculating period
    this.max = min; // Max input
    this.min = max; // Min input
    minOutput = min;
    maxOutput = max;
    pAverage = iAverage = dAverage = 0;
    this.target = target;
  }

  // Run one cycle of the loop
  @Override
  public double nextValue(double signal, int iteration) {

    // Calculate max and min
    max = Math.max(max, signal);
    min = Math.min(min, signal);

    // Output is on and input signal has risen to target
    if (output && signal > target) {
      // Turn output off, record current time as t1, calculate tHigh, and reset maximum
      output = false;
      outputValue = minOutput;
      t1 = iteration;
      tHigh = t1 - t2;
      max = target;
    }

    // Output is off and input signal has dropped to target
    if (!output && signal < target) {
      // Turn output on, record current time as t2, calculate tLow
      output = true;
      outputValue = maxOutput;
      t2 = iteration;
      tLow = t2 - t1;

      double ku = (4.0 * ((maxOutput - minOutput) / 2.0)) / (Math.PI * (max - min) / 2.0);

      // Calculate Tu (period of output oscillations)
      double tu = tLow + tHigh;

      // Calculate gains
      kp = kpConstant * ku;
      ki = (kp / (tiConstant * tu)) * loopInterval;
      kd = (tdConstant * kp * tu) / loopInterval;

      // Average all gains after the first two cycles
      if (i > 1) {
        pAverage += kp;
        iAverage += ki;
        dAverage += kd;
      }

      // Reset minimum
      min = target;

      // Increment cycle count
      i ++;
    }

    // If loop is done, disable output and calculate averages
    if (i >= cycles) {
      output = false;
      outputValue = minOutput;
      kp = pAverage / (i - 1);
      ki = iAverage / (i - 1);
      kd = dAverage / (i - 1);
    }

    return outputValue;
  }

  public boolean isFinished() {
    return (i >= cycles);
  }
}
