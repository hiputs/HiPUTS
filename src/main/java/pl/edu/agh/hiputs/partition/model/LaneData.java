package pl.edu.agh.hiputs.partition.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class LaneData {

  @Builder.Default
  private final String id = UUID.randomUUID().toString();
  @Builder.Default
  @EqualsAndHashCode.Exclude
  private final List<LaneData> availableSuccessors = new ArrayList<>();

}
