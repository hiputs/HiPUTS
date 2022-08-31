package pl.edu.agh.hiputs.loadbalancer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class PID {

  private double kP = 1.0;
  private double kI = 1.0;
  private double kD = 1.0;

  private double startValue_kP = 1.0;
  private double startValue_kI = 1.0;
  private double startValue_kD = 1.0;
  private double lastError = 0;
  private double integralError = 0;

  private static final double dt = 1d; //actual simulation step is 1 second

  @Setter
  @Getter
  private double target = 0;

  private PID(double kP, double kI, double kD, double target) {
    this.kP = kP;
    this.kI = kI;
    this.kD = kD;
    startValue_kP = kP;
    startValue_kD = kD;
    startValue_kI = kI;
    this.target = target;
  }

  public double nextValue(final double currentValue) {
    final double error = target - currentValue;

    // Compute Integral & Derivative error
    final double derivativeError = (error - lastError) / dt;
    integralError += error * dt;

    // Save history
    lastError = error;

    return (kP * error) + (kI * integralError) + (kD * derivativeError);
  }

  public void reset(){
    kD = startValue_kD;
    kP = startValue_kP;
    kI = startValue_kI;
    lastError = 0;
    integralError = 0;
  }
}
