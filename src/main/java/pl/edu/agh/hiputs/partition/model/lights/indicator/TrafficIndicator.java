package pl.edu.agh.hiputs.partition.model.lights.indicator;

import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.edu.agh.hiputs.partition.model.lights.LightColor;

@Getter
@Builder
@EqualsAndHashCode
public class TrafficIndicator implements TrafficIndicatorEditable{
  @Builder.Default
  private final String id = UUID.randomUUID().toString();
  @Builder.Default
  @EqualsAndHashCode.Exclude
  private LightColor currentColor = LightColor.RED;

  @Override
  public void switchColor(LightColor newColor) {
    this.currentColor = newColor;
  }
}
