package pl.edu.agh.hiputs.partition.model.lights.indicator;

import pl.edu.agh.hiputs.partition.model.lights.LightColor;

public class TrafficIndicator implements TrafficIndicatorEditable{
  private LightColor currentColor = LightColor.RED;

  @Override
  public void switchColor(LightColor newColor) {
    this.currentColor = newColor;
  }

  @Override
  public LightColor getCurrentColor() {
    return currentColor;
  }
}
