package pl.edu.agh.hiputs.service.routegenerator.generator;

import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.sql.Timestamp;
import java.util.List;

public interface FileInputGenerator {

    /**
     * This method generates file with routes for entire simulation for single patch
     * routes will be hold in different files for every patch but in one dictionary
     * This way every 'worker' can generate routes for his patches parallelly
     * It will read generatorConf.json to be able to infer how many routes generate
     * It returns name of file
     * @return
     */
    List<RouteWithLocation> generateRouteFileInput(Patch patch, Timestamp startTime, Timestamp endTime, MapRepository mapRepository);

}
