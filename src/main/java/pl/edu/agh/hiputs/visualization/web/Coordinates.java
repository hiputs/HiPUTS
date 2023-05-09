package pl.edu.agh.hiputs.visualization.web;

import java.util.List;

public class Coordinates {

    private List<Double> coordinates;

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public Coordinates(double latitude, double longitude) {
        coordinates = List.of(latitude,longitude);
    }


}