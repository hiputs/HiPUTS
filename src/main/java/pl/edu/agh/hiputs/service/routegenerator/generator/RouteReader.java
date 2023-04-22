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
  Optional<RouteWithLocation> readNextRoute(PatchId patchId, int step) {
    var lineOpt = Optional.ofNullable(fileCursors.get(patchId)).flatMap(lineNumber -> readLineFromFile(patchId, lineNumber));
    if (lineOpt.isEmpty())
      removeFileCursor(patchId);
    var route = lineOpt.flatMap(line -> parseRoute(line, step));
    if (route.isPresent())
      updateFileCursor(patchId);
    return route;
  }

  private Optional<String> readLineFromFile(PatchId patch, int lineNumber) {
    var filePath = format("{0}/patch_{1}", directoryPath, patch.getValue());
    try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
      return lines.skip(lineNumber).findFirst();
    } catch (IOException e) {
      log.error("Error while reading file: " + filePath, e);
      return Optional.empty();
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
        var junctionId = new JunctionId(routeUUID.get(i + 1), JunctionType.CROSSROAD);
        var laneID = new LaneId(routeUUID.get(i));
        // arbitralnie crossroad, trzeba sie zastanowic, jak to ogarnac
        routeElements.add(new RouteElement(junctionId, laneID));
      }
      return new RouteWithLocation(routeElements, 0);
    });
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
