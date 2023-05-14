package pl.edu.agh.hiputs.partition.model.lights.control;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.edu.agh.hiputs.partition.model.lights.group.GreenColorGroupEditable;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class StandardSignalsControlCenter implements SignalsControlCenter{
  private final String id;
  private final int durationSteps;
  @EqualsAndHashCode.Exclude
  // list brings order ability (maybe needed in the future)
  private final List<GreenColorGroupEditable> greenColorGroups;
  @Setter
  private int currentTime = 0;

  public StandardSignalsControlCenter(int durationSteps) {
    this(UUID.randomUUID().toString(), durationSteps, new ArrayList<>());
  }
}
