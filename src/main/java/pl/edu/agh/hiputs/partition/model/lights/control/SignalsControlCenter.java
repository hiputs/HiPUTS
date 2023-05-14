package pl.edu.agh.hiputs.partition.model.lights.control;

import java.util.List;
import pl.edu.agh.hiputs.partition.model.lights.group.GreenColorGroupEditable;

public interface SignalsControlCenter {

  String getId();

  int getDurationTime();

  List<GreenColorGroupEditable> getGreenColorGroups();
}
