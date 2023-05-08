package pl.edu.agh.hiputs.partition.model.lights.group;

import pl.edu.agh.hiputs.partition.model.lights.LightColor;

public interface GreenColorGroupEditable extends GreenColorGroupReadable {

  void switchColorForAll(LightColor newColor);
}
