package pl.edu.agh.hiputs.service.routegenerator.generator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LiveGeneratorCarAmountProvider {

  private final TimeBasedCarGeneratorConfig timeBasedCarGeneratorConfig;

  public int getCarsToGenerateAmountAtStep(long step, double patchTotalLaneLength) {
    return timeBasedCarGeneratorConfig.getCarsPerLaneAtStep(step).map(intensity -> {
      var carsAmountToGenerateInInterval = patchTotalLaneLength / 1000 * intensity.getCarsPerKmLane();
      var averageCarPerStep = carsAmountToGenerateInInterval / intensity.getDuration();
      var carsAtPreviousStep = averageCarPerStep * (step - 1);
      var carsAtCurrentStep = averageCarPerStep * step;
      return (int) (Math.floor(carsAtCurrentStep) - Math.floor(carsAtPreviousStep));
    }).orElse(0);
  }

}
