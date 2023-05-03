package pl.edu.agh.hiputs.partition.model.geom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VectorTest {
  @Test
  public void calculateAngleBetweenSameVector() {
    // given
    Vector vector = new Vector(1, 1);

    // when

    // then
    Assertions.assertTrue(Vector.calculateAngleBetween(vector, vector) - 0.0 < 10e-6);  // Math.sqrt precision
  }

  @Test
  public void calculateAngleBetweenDifferentVectors() {
    // given
    Vector vector1 = new Vector(1, 1);
    Vector vector2 = new Vector(1, -1);

    // when

    // then
    Assertions.assertEquals(90, Vector.calculateAngleBetween(vector1, vector2));
  }

  @Test
  public void calculateAngleBetweenVectorsMoreThan180() {
    // given
    Vector vector1 = new Vector(1, 1);
    Vector vector2 = new Vector(-1, 1);

    // when

    // then
    Assertions.assertEquals(270, Vector.calculateAngleBetween(vector1, vector2));
  }
}
