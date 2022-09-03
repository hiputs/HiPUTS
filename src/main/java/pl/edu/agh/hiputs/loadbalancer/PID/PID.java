package pl.edu.agh.hiputs.loadbalancer.PID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.hiputs.loadbalancer.PID.PidAutoTuner.PIDParameters;

@NoArgsConstructor
public class PID {

  private double kP = 1.0;
  private double kI = 1.0;
  private double kD = 1.0;

  private double lastError = 0;
  private double integralError = 0;

  private int iteration = 0;

  private PidAutoTuner tuner;

  public static final int INITIALIZATION_STEP = 10;

  public PID(double target) {
    this.target = target;
  }

  public PID(PidAutoTuner tuner, double target) {
    this.tuner = tuner;
    this.target = target;
    tuner.reset(INITIALIZATION_STEP, target);
  }

  private PID(double kP, double kI, double kD, double target) {
    this.kP = kP;
    this.kI = kI;
    this.kD = kD;
    this.target = target;
  }

  private static final double dt = 1d; //actual simulation step is 1 second

  @Setter
  @Getter
  private double target = 0;

  public double nextValue(final double currentValue) {
    final double error = target - currentValue;

    // Compute Integral & Derivative error
    final double derivativeError = (error - lastError) / dt;
    integralError += error * dt;

    // Save history
    lastError = error;

    if(tuner != null) {
      tuner.nextValue(currentValue, iteration);

      if (tuner.isFinished()){
        final PIDParameters parameters = tuner.getParameters();
        kP = parameters.getKP();
        kD = parameters.getKD();
        kI = parameters.getKI();
        tuner.reset(30, target);
      }

    }

    iteration++;

    return (kP * error) + (kI * integralError) + (kD * derivativeError);
  }
}
