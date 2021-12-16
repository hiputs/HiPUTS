package model.car;

import lombok.Data;

@Data
public class CarData {
    private double position;
    private double speed;
    private final double length = 5.0;// #TODO set parameter
    private final double maxSpeed = 20.0;// #TODO set parameter

    public CarData(double position, double speed){
        this.position = position;
        this.speed = speed;
    }
}
