package pl.edu.agh.hiputs.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarRead;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.Lane;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


class LaneTest {
    private Lane lane;
    private double lane_length = 1000.0;
    private double car1_speed = 10.4;
    private double car1_pos = 153.2;
    private double car2_speed = 15.2;
    private double car2_pos = 262.4;
    private double car3_speed = 14.2;
    private double car3_pos = 563.2;
    private Car car1;
    private Car car2;
    private Car car3;

    private Car createCar(LaneId currentLaneId, double positionOnLane, double speed){
        return Car.builder()
                .id(new CarId())
                .laneId(currentLaneId)
                .positionOnLane(positionOnLane)
                .speed(speed)
                .build();
    }

    @BeforeEach
    void setupLane() {
        lane = new Lane();
        lane.setLength(lane_length);

        car1 = createCar(lane.getId(), car1_pos, car1_speed);
        car2 = createCar(lane.getId(), car2_pos, car2_speed);
        car3 = createCar(lane.getId(), car3_pos, car3_speed);

        lane.addFirstCar(car3);
        lane.addFirstCar(car2);
        lane.addFirstCar(car1);
    }

    @Test
    void getNextCarData() {
        Optional<CarRead> optional1 = lane.getNextCarData(car1);
        Optional<CarRead> optional2 = lane.getNextCarData(car2);
        assertAll(
                () -> assertFalse(optional1.isEmpty()),
                () -> assertEquals(optional1.get().getPositionOnLane(), car2_pos),
                () -> assertEquals(optional1.get().getSpeed(), car2_speed),
                () -> assertFalse(optional2.isEmpty()),
                () -> assertEquals(optional2.get().getPositionOnLane(), car3_pos),
                () -> assertEquals(optional2.get().getSpeed(), car3_speed)
        );
    }

    @Test
    void getNextCarEmpty() {
        Optional<CarRead> optional = lane.getNextCarData(car3);
        assertTrue(optional.isEmpty());
    }

    @Test
    void getFirstCar() {
        Optional<CarRead> optional = lane.getFirstCar();
        assertAll(
                () -> assertFalse(optional.isEmpty()),
                () -> assertEquals(optional.map(CarRead::getPositionOnLane).get(), car1_pos),
                () -> assertEquals(optional.map(CarRead::getSpeed).get(), car1_speed)
        );
    }

    @Test
    void getFirstCarEmpty() {
        Lane emptyLane = new Lane();
        Optional<CarRead> optional = emptyLane.getFirstCar();
        assertTrue(optional.isEmpty());
    }

    @Disabled("Validation of adding the same car will be added in the future")
    @Test
    void addToIncomingCarsTwice() {
        assertDoesNotThrow(() -> lane.addToIncomingCars(car1));
        assertThrows(Exception.class, () -> lane.addToIncomingCars(car1));
    }
}