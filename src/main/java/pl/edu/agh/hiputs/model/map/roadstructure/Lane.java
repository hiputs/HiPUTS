package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;

@Slf4j
@Builder
@AllArgsConstructor
public class Lane implements LaneEditable{

  @Getter
  private final LaneId laneId;

  @Getter
  private final RoadId roadId;

  @Builder.Default
  private final List<LaneId> laneSuccessors = new ArrayList<>();
}
