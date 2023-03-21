package pl.edu.agh.hiputs.partition.model;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class LaneData {
  private List<LaneData> availableSuccessors;
}
