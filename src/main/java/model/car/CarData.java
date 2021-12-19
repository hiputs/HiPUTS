package model.car;

import lombok.Data;

@Data
public class CarData {
    private double position;
    private double speed;
    private final double length;    // #TODO check value of parameter
    private final double maxSpeed;  // #TODO check value of parameter


    public CarData(double position, double speed){
        this.position = position;
        this.speed = speed;
        this.length = 5.0;
        this.maxSpeed = 20.0;
    }

    public CarData(double position, double speed, double length, double maxSpeed){
        this.position = position;
        this.speed = speed;
        this.length = length;
        this.maxSpeed = maxSpeed;
    }
}
