package pl.edu.agh.hiputs.partition.model.lights.group;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.model.lights.LightColor;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorEditable;

@RequiredArgsConstructor
public class MultipleTIsGreenColorGroup implements GreenColorGroup {
  private final Collection<TrafficIndicatorEditable> trafficIndicators;

  @Override
  public void switchColorForAll(LightColor newColor) {
    trafficIndicators.forEach(indicator -> indicator.switchColor(newColor));
  }
}
