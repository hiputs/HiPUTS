package pl.edu.agh.hiputs.partition.model.lights.control;

import java.util.ArrayList;
import java.util.List;
import pl.edu.agh.hiputs.partition.model.lights.group.GreenColorGroup;

public class StandardSignalsControlCenter implements SignalsControlCenter{
  private final List<GreenColorGroup> greenColorGroups = new ArrayList<>();

  @Override
  public List<GreenColorGroup> getGreenColorGroups() {
    return greenColorGroups;
  }
}
