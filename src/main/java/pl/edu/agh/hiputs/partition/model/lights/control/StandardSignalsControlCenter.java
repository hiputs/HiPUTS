package pl.edu.agh.hiputs.partition.model.lights.control;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.edu.agh.hiputs.partition.model.lights.group.GreenColorGroupEditable;

@Getter
@Builder
@EqualsAndHashCode
public class StandardSignalsControlCenter implements SignalsControlCenter{
  @Builder.Default
  private final String id = UUID.randomUUID().toString();
  @Builder.Default
  @EqualsAndHashCode.Exclude
  // list brings order ability (maybe needed in the future)
  private final List<GreenColorGroupEditable> greenColorGroups = new ArrayList<>();
}
