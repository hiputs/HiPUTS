package pl.edu.agh.hiputs.partition.model.lights.indicator;

import pl.edu.agh.hiputs.partition.model.lights.LightColor;

public interface TrafficIndicatorEditable extends TrafficIndicatorReadable{

  void switchColor(LightColor newColor);
}
