package pl.edu.agh.hiputs.partition.model.lights.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.edu.agh.hiputs.partition.model.lights.LightColor;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorEditable;

@Getter
@Builder
@EqualsAndHashCode
public class MultipleTIsGreenColorGroup implements GreenColorGroup {
  @Builder.Default
  private final String id = UUID.randomUUID().toString();
  @Builder.Default
  @EqualsAndHashCode.Exclude
  private Collection<TrafficIndicatorEditable> trafficIndicators = new ArrayList<>();

  @Override
  public void switchColorForAll(LightColor newColor) {
    trafficIndicators.forEach(indicator -> indicator.switchColor(newColor));
  }
}
