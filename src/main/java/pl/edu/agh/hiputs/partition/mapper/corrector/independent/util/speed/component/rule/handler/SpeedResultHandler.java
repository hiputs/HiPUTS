package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler;

import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Getter
@Setter
public class SpeedResultHandler {
  private Edge<JunctionData, WayData> edge;
  private int resultSpeed;
  private String country;
  private TypeOfRoad typeOfRoad;
}
