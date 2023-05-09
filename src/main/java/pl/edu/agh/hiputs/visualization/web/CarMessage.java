package pl.edu.agh.hiputs.visualization.web;

import java.util.List;

public class CarMessage {
    String carId;
    double length;
    double acceleration;
    double speed;
    double maxSpeed;
    String node1Id;
    String node2Id;
    double positionOnLane;

   List<Double> position;

    public void setPosition(List<Double> position) {
        this.position = position;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public String getNode1Id() {
        return node1Id;
    }

    public void setNode1Id(String node1Id) {
        this.node1Id = node1Id;
    }

    public String getNode2Id() {
        return node2Id;
    }

    public void setNode2Id(String node2Id) {
        this.node2Id = node2Id;
    }

    public double getPositionOnLane() {
        return positionOnLane;
    }

    public void setPositionOnLane(double positionOnLane) {
        this.positionOnLane = positionOnLane;
    }

    public List<Double> getPosition() {
        return position;
    }
}
