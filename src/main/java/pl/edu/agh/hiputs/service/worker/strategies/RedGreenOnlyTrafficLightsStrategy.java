package pl.edu.agh.hiputs.service.worker.strategies;

import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.lights.LightColor;
import pl.edu.agh.hiputs.partition.model.lights.control.SignalsControlCenter;

@Service
public class RedGreenOnlyTrafficLightsStrategy implements TrafficLightsStrategy {

  @Override
  public void execute(SignalsControlCenter signalsControlCenter) {
    if (signalsControlCenter.getCurrentTime() < signalsControlCenter.getDurationSteps()) {
      signalsControlCenter.setCurrentTime(signalsControlCenter.getCurrentTime() + 1);
    } else {
      signalsControlCenter.setCurrentTime(0);
      changeColorBetweenRedAndGreen(signalsControlCenter);
    }
  }

  private void changeColorBetweenRedAndGreen(SignalsControlCenter signalsControlCenter) {
    int currentGreenGroupIndex = signalsControlCenter.getCurrentGreenGroupIndex();

    signalsControlCenter.getGreenColorGroups().get(currentGreenGroupIndex).switchColorForAll(LightColor.RED);
    currentGreenGroupIndex = (currentGreenGroupIndex + 1) % signalsControlCenter.getGreenColorGroups().size();
    signalsControlCenter.getGreenColorGroups().get(currentGreenGroupIndex).switchColorForAll(LightColor.GREEN);

    signalsControlCenter.setCurrentGreenGroupIndex(currentGreenGroupIndex);
  }
}
