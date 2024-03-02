package pl.edu.agh.hiputs.partition.model.lights.indicator;

import pl.edu.agh.hiputs.partition.model.lights.LightColor;

public interface TrafficIndicatorReadable {

  String getId();

  LightColor getCurrentColor();
}