package pl.edu.agh.hiputs.partition.service.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Line given by standard equation: Ax + By + C = 0
 */
@Data
@AllArgsConstructor
public class StandardEquationLine {

  private double A;
  private double B;
  private double C;

}
