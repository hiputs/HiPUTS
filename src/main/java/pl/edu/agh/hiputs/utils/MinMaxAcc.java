package pl.edu.agh.hiputs.utils;

import lombok.Getter;

@Getter
public class MinMaxAcc {

  private Double min = Double.MAX_VALUE;
  private Double max = Double.MIN_VALUE;

  public MinMaxAcc() {

  }

  public void accept(Double val) {
    if (val < min) {
      min = val;
    } else if (val > max) {
      max = val;
    }
  }

  public Double getRange() {
    return max - min;
  }

  public Double getMiddle() {
    return min + getRange() / 2;
  }
}
