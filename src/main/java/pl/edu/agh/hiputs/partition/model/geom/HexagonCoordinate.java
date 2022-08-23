package pl.edu.agh.hiputs.partition.model.geom;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HexagonCoordinate {

  private int xHex;
  private int yHex;

}