package pl.edu.agh.hiputs.partition.model.lights.control;

import java.util.List;
import pl.edu.agh.hiputs.partition.model.lights.group.GreenColorGroup;

public interface SignalsControlCenter {

  List<GreenColorGroup> getGreenColorGroups();
}
