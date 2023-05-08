package pl.edu.agh.hiputs.partition.model.lights.group;

import pl.edu.agh.hiputs.partition.model.lights.LightColor;

public interface GreenColorGroup {

  String getId();

  void switchColorForAll(LightColor newColor);
}
