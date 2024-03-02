package pl.edu.agh.hiputs.partition.model.lights.group;

import java.util.Collection;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorEditable;

public interface GreenColorGroupReadable {

  String getId();

  Collection<TrafficIndicatorEditable> getTrafficIndicators();
}
