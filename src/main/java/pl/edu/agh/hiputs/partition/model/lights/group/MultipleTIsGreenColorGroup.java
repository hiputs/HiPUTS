package pl.edu.agh.hiputs.partition.model.lights.group;

import java.util.List;
import pl.edu.agh.hiputs.partition.model.lights.LightColor;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorEditable;

public class MultipleTIsGreenColorGroup implements GreenColorGroup {
  private final List<TrafficIndicatorEditable> trafficIndicators;

  private MultipleTIsGreenColorGroup(TrafficIndicatorEditable ... trafficIndicators) {
    this.trafficIndicators = List.of(trafficIndicators);
  }

  @Override
  public void switchColorForAll(LightColor newColor) {
    trafficIndicators.forEach(indicator -> indicator.switchColor(newColor));
  }
}
