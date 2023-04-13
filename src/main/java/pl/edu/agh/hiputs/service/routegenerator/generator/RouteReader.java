package pl.edu.agh.hiputs.service.routegenerator.generator;

import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.service.routegenerator.UnimplementedException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RouteReader {

    private final Map<PatchId, Integer> fileCursors;

    public RouteReader() {
        File file = null;
        // ten generatorConf.json wrzuciłbym do resources
        // chociaz zastanawiam sie czy skoro to spring to nie lepiej wrzucic do springowej konfiugracji, ale to do przegadania
        // reading from file
        fileCursors = new HashMap<>();
        throw new UnimplementedException();
    }

    /**
     * This method reads next line from file which holds routes for certain patch
     * then returns route for new Car and increments lineCounter for this patch
     * it is used to generate route for generated car in simulation
     */
    Optional<RouteWithLocation> readNextRoute(PatchId patchId) {
        var routeWithLocationOpt = Optional.ofNullable(fileCursors.get(patchId)).flatMap(lineNumber -> readRouteFromFile(patchId, lineNumber));
        if (routeWithLocationOpt.isPresent())
            updateFileCursor(patchId);
        else
            removeFileCursor(patchId);
        return routeWithLocationOpt;
    }

    private Optional<RouteWithLocation> readRouteFromFile(PatchId patch, int lineNumber) {
        throw new UnimplementedException();
    }

    private void updateFileCursor(PatchId patchId) {
        fileCursors.put(patchId, fileCursors.get(patchId) + 1);
    }

    private void removeFileCursor(PatchId patchId) {
        fileCursors.remove(patchId);
    }
}
