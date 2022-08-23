package pl.edu.agh.hiputs.partition.model.geom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class HexagonCoordinate {

  private int xHex;
  private int yHex;

}