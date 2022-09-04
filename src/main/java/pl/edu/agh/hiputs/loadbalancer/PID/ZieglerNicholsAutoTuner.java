package pl.edu.agh.hiputs.loadbalancer.PID;

import lombok.Setter;

// Adaptation C/C++ algorithm https://github.com/jackw01/arduino-pid-autotuner
public class ZieglerNicholsAutoTuner implements PidAutoTuner {

  @Setter
  private double target = 0;
  private static final double loopInterval = 1;
  private static final double minOutput = -1000d, maxOutput = 1000d;

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

  @Override
  public PIDParameters getParameters() {
    return new PIDParameters(kp, ki, kd);
  }

  @Override
  public void nextValue(double input, int iteration) {
    // Useful information on the algorithm used (Ziegler-Nichols method/Relay method)
    // http://www.processcontrolstuff.net/wp-content/uploads/2015/02/relay_autot-2.pdf
    // https://en.wikipedia.org/wiki/Ziegler%E2%80%93Nichols_method
    // https://www.cds.caltech.edu/~murray/courses/cds101/fa04/caltech/am04_ch8-3nov04.pdf

    // Basic explanation of how this works:
    //  * Turn on the output of the PID controller to full power
    //  * Wait for the output of the system being tuned to reach the target input value
    //      and then turn the controller output off
    //  * Wait for the output of the system being tuned to decrease below the target input
    //      value and turn the controller output back on
    //  * Do this a lot
    //  * Calculate the ultimate gain using the amplitude of the controller output and
    //      system output
    //  * Use this and the period of oscillation to calculate PID gains using the
    //      Ziegler-Nichols method

    // Calculate time delta
    //float deltaT = microseconds - prevMicroseconds;

    // Calculate max and min
    max = Math.max(max, input);
    min = Math.min(min, input);

    // Output is on and input signal has risen to target
    if (output && input > target) {
      // Turn output off, record current time as t1, calculate tHigh, and reset maximum
      output = false;
      t1 = iteration;
      tHigh = t1 - t2;
      max = target;
    }

    // Output is off and input signal has dropped to target
    if (!output && input < target) {
      // Turn output on, record current time as t2, calculate tLow
      output = true;
      t2 = iteration;
      tLow = t2 - t1;

      // Calculate Ku (ultimate gain)
      // Formula given is Ku = 4d / πa
      // d is the amplitude of the output signal
      // a is the amplitude of the input signal
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
      i++;
    }

    // If loop is done, disable output and calculate averages
    if (i >= cycles) {
      output = false;
      kp = pAverage / (i - 1);
      ki = iAverage / (i - 1);
      kd = dAverage / (i - 1);
    }
  }

  // Initialize all variables before loop
  public void reset(int cycles, double target, int iteration) {
    i = 0; // Cycle counter
    output = true; // Current output state
    t1 = t2 = iteration; // Times used for calculating period
    max = -1000000000000d; // Max input
    min = 1000000000000d; // Min input
    pAverage = iAverage = dAverage = 0;
    kp = 0;
    ki = 0;
    kd = 0;
    this.cycles = cycles;
    this.target = target;
  }

  public boolean isFinished() {
    return (i >= cycles);
  }
}
