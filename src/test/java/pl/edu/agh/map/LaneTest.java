package pl.edu.agh.map;

import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.car.CarReadOnly;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.model.map.Lane;
import pl.edu.agh.model.map.LaneReadOnly;

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

    @BeforeEach
    void setupLane(){
        lane = new Lane();
        lane.setLength(lane_length);
        car1 = new Car();
        car2 = new Car();
        car3 = new Car();
        car1.setPosition(car1_pos);
        car1.setSpeed(car1_speed);
        car2.setPosition(car2_pos);
        car2.setSpeed(car2_speed);
        car3.setPosition(car3_pos);
        car3.setSpeed(car3_speed);
        lane.addFirstCar(car3);
        lane.addFirstCar(car2);
        lane.addFirstCar(car1);
    }

    @Test
    void getNextCarData() {
        Optional<CarReadOnly> optional1 = lane.getNextCarData(car1);
        Optional<CarReadOnly> optional2 = lane.getNextCarData(car2);
        assertAll(
                () -> assertFalse(optional1.isEmpty()),
                () -> assertEquals(optional1.get().getPosition(), car2_pos),
                () -> assertEquals(optional1.get().getSpeed(), car2_speed),
                () -> assertFalse(optional2.isEmpty()),
                () -> assertEquals(optional2.get().getPosition(), car3_pos),
                () -> assertEquals(optional2.get().getSpeed(), car3_speed)
        );
    }

    @Test
    void getNextCarEmpty() {
        Optional<CarReadOnly> optional = lane.getNextCarData(car3);
        assertTrue(optional.isEmpty());
    }

    @Test
    void getFirstCar() {
        Optional<CarReadOnly> optional = lane.getFirstCar();
        assertAll(
                () -> assertFalse(optional.isEmpty()),
                () -> assertEquals(optional.map(car -> car.getPosition()).get(), car1_pos),
                () -> assertEquals(optional.map(car -> car.getSpeed()).get(), car1_speed)
        );
    }

    @Test
    void getFirstCarEmpty() {
        Lane emptyLane = new Lane();
        Optional<CarReadOnly> optional = emptyLane.getFirstCar();
        assertTrue(optional.isEmpty());
    }
}