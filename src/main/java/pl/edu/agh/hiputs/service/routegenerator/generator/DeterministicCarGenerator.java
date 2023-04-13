package pl.edu.agh.hiputs.service.routegenerator.generator;

import lombok.AllArgsConstructor;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.routegenerator.UnimplementedException;

import java.util.List;

@AllArgsConstructor
public class DeterministicCarGenerator implements CarGenerator{

    private final RouteReader routeReader;

    @Override
    public List<RouteWithLocation> generateRoutes(Patch patch) {
        throw new UnimplementedException();
    }
}
