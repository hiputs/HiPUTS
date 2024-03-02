package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.processor;

import java.util.List;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.TurnDirection;
import pl.edu.agh.hiputs.partition.model.WayData;

public interface TurnProcessor {

  List<List<TurnDirection>> getTurnDirectionsFromTags(WayData wayData);

}
