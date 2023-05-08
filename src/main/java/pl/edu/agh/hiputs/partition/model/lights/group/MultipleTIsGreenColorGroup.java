package pl.edu.agh.hiputs.partition.model.lights.group;

import java.util.Collection;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.model.lights.LightColor;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorEditable;

@EqualsAndHashCode
@RequiredArgsConstructor
public class MultipleTIsGreenColorGroup implements GreenColorGroup {
  private final String id;
  @EqualsAndHashCode.Exclude
  private final Collection<TrafficIndicatorEditable> trafficIndicators;

  public MultipleTIsGreenColorGroup(Collection<TrafficIndicatorEditable> trafficIndicators) {
    this(UUID.randomUUID().toString(), trafficIndicators);
  }

  @Override
  public void switchColorForAll(LightColor newColor) {
    trafficIndicators.forEach(indicator -> indicator.switchColor(newColor));
  }
}
