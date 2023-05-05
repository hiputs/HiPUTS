package pl.edu.agh.hiputs.partition.model.lights.group;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.model.lights.LightColor;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorEditable;

@RequiredArgsConstructor
public class MultipleTIsGreenColorGroup implements GreenColorGroup {
  private final List<TrafficIndicatorEditable> trafficIndicators;

  @Override
  public void switchColorForAll(LightColor newColor) {
    trafficIndicators.forEach(indicator -> indicator.switchColor(newColor));
  }
}
