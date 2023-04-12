package pl.edu.agh.hiputs.routemodule.generator;

import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.patch.Patch;

import java.sql.Timestamp;
import java.util.ArrayList;

public interface LiveGenerator {

    /**
     * This method generates routes for single patch in given time period to add as cars that meant to be created
     * It will read generatorConf.json to be able to infer how many routes generate
     * It generates routes with increasing start times.
     */
    ArrayList<RouteWithLocation> generateRoutes(Patch patch, Timestamp startTime, Timestamp endTime);
}
