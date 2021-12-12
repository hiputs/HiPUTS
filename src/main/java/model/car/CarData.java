package model.car;

import lombok.Data;

@Data
public class CarData {
    private double position;
    private double speed;

    public CarData(double position, double speed){
        this.position = position;
        this.speed = speed;
    }
}
