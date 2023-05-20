package pl.edu.agh.hiputs.service.routegenerator.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.service.ConfigurationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static pl.edu.agh.hiputs.utils.CollectionUtil.optionalWhen;

@Slf4j
@Component
public class RouteReader {

  private final String directoryPath;
  private final Map<PatchId, Integer> fileCursors = new HashMap<>();
  private final int stepTimeMs;


  public RouteReader(@Value("${carGenerator.filesPath}") String directoryPath) {
    this.directoryPath = directoryPath;
    File directory = new File(directoryPath);
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile() && file.getName().startsWith("patch_")) {
          String patchIdString = file.getName().substring(6);
          fileCursors.put(new PatchId(patchIdString), 0);
        }
      }
    }
    this.stepTimeMs = ConfigurationService.getConfiguration().getSimulationStep();

  }

  /**
   * This method reads next line from file which holds routes for certain patch
   * then returns route for new Car and increments lineCounter for this patch
   * it is used to generate route for generated car in simulation
   */
  List<RouteWithLocation> readNextRoutes(PatchId patchId, int step) {
    List<RouteWithLocation> nextRoutes = new ArrayList<>();
    var filePath = format("{0}/patch_{1}", directoryPath, patchId.getValue());
    try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
      var lineNumber = Optional.ofNullable(fileCursors.get(patchId));
      if (lineNumber.isPresent()){
        lines.skip(lineNumber.get()).takeWhile(line -> !isRouteFuture(line, step)).forEach(line -> {
          updateFileCursor(patchId);
          var parsed = getRoute(line, step);
          parsed.ifPresent(nextRoutes::add);
        });
      }
      else{
        removeFileCursor(patchId);
      }
      return nextRoutes;
    } catch (IOException e) {
      log.error("Error while reading file: " + filePath, e);
      return nextRoutes;
    }
  }

  private Optional<RouteWithLocation> parseRoute(String line, int step) {
      var tokens = line.split(";");
      var creationTimeMs = Long.parseLong(tokens[0]);
      var startLaneUUID = UUID.fromString(tokens[1]);
      var endLaneUUID = UUID.fromString(tokens[2]);
      var routeUUID = Stream.of(tokens[3].split(",")).toList();
      var routeElements = new ArrayList<RouteElement>();
      return optionalWhen(isReadyToBeCreated(creationTimeMs, step), () -> {
        for (int i = 0; i < routeUUID.size(); i += 2) {
          var junctionId = new JunctionId(routeUUID.get(i), JunctionType.CROSSROAD);
          var laneID = new LaneId(routeUUID.get(i+1));
          // arbitralnie crossroad, trzeba sie zastanowic, jak to ogarnac
          routeElements.add(new RouteElement(junctionId, laneID));
        }
        return new RouteWithLocation(routeElements, 0);
      });
  }

  /**
   * this method doesn't log error becouse we want to actualy skip incorect line and find last that is there to be
   * read.The error will be logged in getRoute function, and this line will not be taken as route to generate car
   */
  private Boolean isRouteFuture(String line, int step){
    try{
      Optional<RouteWithLocation> parsed = parseRoute(line, step);
      if (parsed.isPresent()){
        return false;
      }
    } catch (IllegalArgumentException e ){
      return false;
    }
    return true;
  }


  private Optional<RouteWithLocation> getRoute(String line, int step) {
    try {
      return parseRoute(line, step);
    } catch (IllegalArgumentException e){
      log.error("Error while parsing line: " + line, e);
      return Optional.empty();
    }
  }

  private boolean isReadyToBeCreated(long creationTimeMs, int step) {
    return (long) step * stepTimeMs >= creationTimeMs;
  }

  private void updateFileCursor(PatchId patchId) {
    fileCursors.put(patchId, fileCursors.get(patchId) + 1);
  }

  private void removeFileCursor(PatchId patchId) {
    fileCursors.remove(patchId);
  }
}
