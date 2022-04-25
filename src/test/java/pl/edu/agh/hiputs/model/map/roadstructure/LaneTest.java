package pl.edu.agh.hiputs.model.map.roadstructure;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.LaneId;

class LaneTest {

  private final double lane_length = 1000.0;
  private final double car1_speed = 10.4;
  private final double car1_pos = 153.2;
  private final double car2_speed = 15.2;
  private final double car2_pos = 262.4;
  private final double car3_speed = 14.2;
  private final double car3_pos = 563.2;
  private Lane lane;
  private Car car1;
  private Car car2;
  private Car car3;

  @BeforeEach
  void setupLane() {
    lane = Lane.builder().length(lane_length).build();

    car1 = createCar(lane.getLaneId(), car1_pos, car1_speed);
    car2 = createCar(lane.getLaneId(), car2_pos, car2_speed);
    car3 = createCar(lane.getLaneId(), car3_pos, car3_speed);

    lane.addCarAtEntry(car3);
    lane.addCarAtEntry(car2);
    lane.addCarAtEntry(car1);
  }

  private Car createCar(LaneId currentLaneId, double positionOnLane, double speed) {
    return Car.builder()
        .carId(CarId.random())
        .laneId(currentLaneId)
        .positionOnLane(positionOnLane)
        .speed(speed)
        .build();
  }

  @Test
  void getNextCarData() {
    Optional<CarReadable> optional1 = lane.getCarInFrontReadable(car1);
    Optional<CarReadable> optional2 = lane.getCarInFrontReadable(car2);
    assertAll(() -> assertFalse(optional1.isEmpty()), () -> assertEquals(optional1.get().getPositionOnLane(), car2_pos),
        () -> assertEquals(optional1.get().getSpeed(), car2_speed), () -> assertFalse(optional2.isEmpty()),
        () -> assertEquals(optional2.get().getPositionOnLane(), car3_pos),
        () -> assertEquals(optional2.get().getSpeed(), car3_speed));
  }

  @Test
  void getNextCarEmpty() {
    Optional<CarReadable> optional = lane.getCarInFrontReadable(car3);
    assertTrue(optional.isEmpty());
  }

  @Test
  void getFirstCar() {
    Optional<CarReadable> optional = lane.getCarAtEntryReadable();
    assertAll(() -> assertFalse(optional.isEmpty()),
        () -> assertEquals(optional.map(CarReadable::getPositionOnLane).get(), car1_pos),
        () -> assertEquals(optional.map(CarReadable::getSpeed).get(), car1_speed));
  }

  @Test
  void getFirstCarEmpty() {
    Lane emptyLane = Lane.builder().build();
    Optional<CarReadable> optional = emptyLane.getCarAtEntryReadable();
    assertTrue(optional.isEmpty());
  }

  @Disabled("Validation of adding the same car will be added in the future")
  @Test
  void addToIncomingCarsTwice() {
    assertDoesNotThrow(() -> lane.addIncomingCar(car1));
    assertThrows(Exception.class, () -> lane.addIncomingCar(car1));
  }
}