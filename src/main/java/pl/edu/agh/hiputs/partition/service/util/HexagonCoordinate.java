package pl.edu.agh.hiputs.partition.service.util;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HexagonCoordinate {

  private int xHex;
  private int yHex;

}