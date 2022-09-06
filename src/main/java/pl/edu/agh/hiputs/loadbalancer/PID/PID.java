package pl.edu.agh.hiputs.loadbalancer.PID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.loadbalancer.PID.PidAutoTuner.PIDParameters;

@Slf4j
@NoArgsConstructor
public class PID {

  private double kP = 0.85;
  private double kI = 3;
  private double kD = 0.75;
  private double lastError = 0;
  private double integralError = 0;

  private double min, max;

  private int iteration = INITIALIZATION_STEP;

  private PidAutoTuner tuner;

  public static final int INITIALIZATION_STEP = 5;

  public PID(double target) {
    this.target = target;
  }

  public PID(PidAutoTuner tuner, double target, double min, double max) {
    this.tuner = tuner;
    this.target = target;
    this.max = max;
    this.min = min;
    tuner.reset(target, INITIALIZATION_STEP, -30, 30);
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

  public double nextValue(double currentValue) {

    // if (tuner != null && !tuner.isFinished()) {  //actually we can't use this because we can only send cars but not download it.
    //   return tuner.nextValue(currentValue, ++iteration);
    // }
    //
    // if(tuner != null && tuner.isFinished()){
    //   final PIDParameters parameters = tuner.getParameters();
    //   kP = parameters.getKP();
    //   kD = parameters.getKD();
    //   kI = parameters.getKI();
    //   tuner = null;
    //   log.debug("New PID params {} {} {}", kP, kD, kI);
    // }

    // if (++iteration < INITIALIZATION_STEP) {
    //   return 0;
    // }

    final double error = target - currentValue;

    // Compute Integral & Derivative error
    final double derivativeError = (error - lastError) / dt;
    integralError += error * dt;

    // Save history
    lastError = error;

    return limit((kP * error) + (kI * integralError) + (kD * derivativeError));
  }

  private double limit(double v) {
    if(v > max){
      return max;
    } else if(v < min){
      return min;
    }

    return v;
  }

  public void setTarget(double target) {
    this.target = target;
    // tuner.setTarget(target);
  }
}
