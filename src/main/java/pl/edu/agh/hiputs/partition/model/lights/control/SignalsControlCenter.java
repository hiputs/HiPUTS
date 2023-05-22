package pl.edu.agh.hiputs.partition.model.lights.control;

import java.util.List;
import pl.edu.agh.hiputs.partition.model.lights.group.GreenColorGroupEditable;

public interface SignalsControlCenter {

  String getId();

  int getDurationSteps();

  int getCurrentTime();

  void setCurrentTime(int currentTime);

  int getCurrentGreenGroupIndex();

  void setCurrentGreenGroupIndex(int currentGroupIndex);

  List<GreenColorGroupEditable> getGreenColorGroups();
}
