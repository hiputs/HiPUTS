package pl.edu.agh.hiputs.partition.model.relation;

import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class Restriction {
  @Builder.Default
  private final String id = UUID.randomUUID().toString();
  private final String fromEdgeId;
  private final String viaNodeId;
  private final String toEdgeId;
  private final RestrictionType type;
}
