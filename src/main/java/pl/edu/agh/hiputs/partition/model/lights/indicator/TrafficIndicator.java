package pl.edu.agh.hiputs.partition.model.lights.indicator;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.model.lights.LightColor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class TrafficIndicator implements TrafficIndicatorEditable{
  private final String id;
  @EqualsAndHashCode.Exclude
  private LightColor currentColor = LightColor.RED;

  public TrafficIndicator() {
    this(UUID.randomUUID().toString());
  }

  @Override
  public void switchColor(LightColor newColor) {
    this.currentColor = newColor;
  }
}
