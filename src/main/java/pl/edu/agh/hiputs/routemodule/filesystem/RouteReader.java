package pl.edu.agh.hiputs.routemodule.filesystem;

import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.patch.Patch;

import java.util.HashMap;

public interface RouteReader {

//    TODO (Michał)
//    jesli czytamy plliki to fajnie jest pamietac dla kazdego na której linijce skończyliśmy
//    żeby nie szukać za każdym razem linijki bo to spowolni, myśle że jeżeli klasa reader ma 1 instancje to powinna
//    przechowywac jakiś dict patch->linijka w pliku,   jak myślisz że lepiej inaczej to zamień

    HashMap<Patch, Integer> nextLines = new HashMap<>();

    /**
     * This method reads next line from file which holds routes for certain patch
     * then returns route for new Car and increments lineCounter for this patch
     * it is used to generate route for generated car in simulation
     */
    RouteWithLocation getNextRoute(Patch patch);


}
