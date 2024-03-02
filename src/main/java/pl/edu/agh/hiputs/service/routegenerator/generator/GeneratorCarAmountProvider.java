package pl.edu.agh.hiputs.service.routegenerator.generator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class GeneratorCarAmountProvider {

  private final TimeBasedCarGeneratorConfig timeBasedCarGeneratorConfig;

  public int getCarsToGenerateAmountAtStep(long step, double patchTotalLaneLength) {
    return timeBasedCarGeneratorConfig.getCarsPerLaneAtStep(step).map(intensity -> {
      var carsAmountToGenerateInInterval = patchTotalLaneLength / 1000 * intensity.getCarsPerKmLane();
      log.debug("carsAmountToGenerateInInterval: {}", carsAmountToGenerateInInterval);
      var averageCarPerStep = carsAmountToGenerateInInterval / intensity.getDuration();
      var carsAtPreviousStep = averageCarPerStep * (step - 1);
      var carsAtCurrentStep = averageCarPerStep * step;
      return (int) (Math.floor(carsAtCurrentStep) - Math.floor(carsAtPreviousStep));
    }).orElse(0);
  }

}
