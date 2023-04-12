package pl.edu.agh.hiputs.routemodule.generator;

import pl.edu.agh.hiputs.model.map.patch.Patch;

import java.sql.Timestamp;

public interface FileGenerator {

    /**
     * This method generates file with routes for entire simulation for single patch
     * routes will be hold in different files for every patch but in one dictionary
     * This way every 'worker' can generate routes for his patches parallelly
     * It will read generatorConf.json to be able to infer how many routes generate
     * It returns name of file
     */
    String generateFileWithRoutes(Patch patch, Timestamp startTime, Timestamp endTime);

}
