package pl.edu.agh.hiputs.loadbalancer.PID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.loadbalancer.PID.PidAutoTuner.PIDParameters;

@Slf4j
@NoArgsConstructor
public class PID {

  private double kP = 0.28;
  private double kI = 0.09;
  private double kD = 0.23;
  private double lastError = 0;
  private double integralError = 0;

  private int iteration = 0;

  private PidAutoTuner tuner;

  public static final int INITIALIZATION_STEP = 5;

  public PID(double target) {
    this.target = target;
  }

  public PID(PidAutoTuner tuner, double target) {
    this.tuner = tuner;
    this.target = target;
    tuner.reset(INITIALIZATION_STEP, target, 0);
  }

  private PID(double kP, double kI, double kD, double target) {
    this.kP = kP;
    this.kI = kI;
    this.kD = kD;
    this.target = target;
  }

  private static final double dt = 1d; //actual simulation step is 1 second

  @Getter
  private double target = 0;

  public double nextValue(final double currentValue) {

    if (tuner != null) {
      tuner.nextValue(currentValue, iteration);

      if (tuner.isFinished()) {
        final PIDParameters parameters = tuner.getParameters();
        kP = parameters.getKP();
        kD = parameters.getKD();
        kI = parameters.getKI();
        log.info("New PID params {} {} {}", kP, kD, kI);
        tuner.reset(30, target, 0);
      }
    }

    iteration++;

    if (iteration < INITIALIZATION_STEP) {
      return 0;
    }

    final double error = target - currentValue;

    // Compute Integral & Derivative error
    final double derivativeError = (error - lastError) / dt;
    integralError += error * dt;

    // Save history
    lastError = error;

    return (kP * error) + (kI * integralError) + (kD * derivativeError);
  }

  public void setTarget(double target) {
    this.target = target;
    tuner.setTarget(target);
  }
}
