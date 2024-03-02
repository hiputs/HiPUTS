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
  @EqualsAndHashCode.Exclude
  private final List<GreenColorGroupEditable> greenColorGroups; // list brings order ability (maybe needed in the future)
  @EqualsAndHashCode.Exclude
  private final int durationSteps;
  @Setter
  @EqualsAndHashCode.Exclude
  private int currentStep = Integer.MAX_VALUE;
  @Setter
  @EqualsAndHashCode.Exclude
  private int currentGreenGroupIndex = 0;

  public StandardSignalsControlCenter(int durationSteps) {
    this(UUID.randomUUID().toString(), new ArrayList<>(), durationSteps);
  }
}
