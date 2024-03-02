package pl.edu.agh.hiputs.service.routegenerator.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.configuration.Configuration;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator.RouteFileEntry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteReader {

  private final Configuration configuration;

  private final Map<PatchId, Integer> fileCursors = new HashMap<>();
  private boolean initializedFileCursors = false;

  /**
   * This method reads next line from file which holds routes for certain patch
   * then returns route for new Car and increments lineCounter for this patch
   * it is used to generate route for generated car in simulation
   */
  List<RouteFileEntry> readNextRoutes(PatchId patchId, int step) {
    if (!initializedFileCursors) {
      initializeFileCursors();
    }
    List<RouteFileEntry> nextRoutes = new ArrayList<>();
    var patchesDirectoryPath = configuration.getCarGenerator().getFilesPath();
    var filePath = format("{0}/patch_{1}", patchesDirectoryPath, patchId.getValue());
    try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
      var lineNumber = Optional.ofNullable(fileCursors.get(patchId));
      if (lineNumber.isPresent()) {
        lines.skip(lineNumber.get())
            .dropWhile(line -> isAlreadyGenerated(line, step, patchId))
            .takeWhile(line -> isCurrentStep(line, step))
            .forEach(line -> {
              safelyParseFileEntry(line).ifPresent(nextRoutes::add);
              updateFileCursor(patchId);
            });
      } else {
        removeFileCursor(patchId);
      }
      return nextRoutes;
    } catch (IOException e) {
      log.error("Error while reading file: " + filePath, e);
      return nextRoutes;
    }
  }

  private void initializeFileCursors() {
    var patchesDirectoryPath = configuration.getCarGenerator().getFilesPath();
    File directory = new File(patchesDirectoryPath);
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile() && file.getName().startsWith("patch_")) {
          String patchIdString = file.getName().substring(6);
          // skipping first line with header
          fileCursors.put(new PatchId(patchIdString), 1);
        }
      }
    }
  }

  private Optional<RouteFileEntry> safelyParseFileEntry(String line) {
    try {
      return Optional.of(parseFileEntry(line));
    } catch (NumberFormatException e) {
      log.error("Error while parsing file entry: " + line, e.getMessage());
      return Optional.empty();
    }
  }

  private RouteFileEntry parseFileEntry(String line) throws NumberFormatException {
    var tokens = line.split(";");
    var creationStep = Long.parseLong(tokens[0]);
    var carLength = Double.parseDouble(tokens[1]);
    var maxSpeed = Double.parseDouble(tokens[2]);
    var speed = Double.parseDouble(tokens[3]);
    var routeUUIDs = Stream.of(tokens[4].split(",")).toList();
    var routeElements = new ArrayList<RouteElement>();
    for (int i = 0; i < routeUUIDs.size(); i += 2) {
      // TODO: change to proper junction type
      var junctionId = new JunctionId(routeUUIDs.get(i), JunctionType.CROSSROAD);
      var roadId = new RoadId(routeUUIDs.get(i + 1));
      routeElements.add(new RouteElement(junctionId, roadId));
    }
    return new RouteFileEntry(creationStep, carLength, maxSpeed, speed, new RouteWithLocation(routeElements, 0));
  }

  private boolean isCurrentStep(String lineStr, long step) {
    return safelyParseFileEntry(lineStr).map(entry -> entry.getStep() == step).orElse(true);
  }

  private boolean isAlreadyGenerated(String lineStr, long step, PatchId patchId) {
    var alreadyGenerated = safelyParseFileEntry(lineStr).map(entry -> entry.getStep() < step).orElse(true);
    if (alreadyGenerated) {
      updateFileCursor(patchId);
    }
    return alreadyGenerated;
  }

  private void updateFileCursor(PatchId patchId) {
    fileCursors.put(patchId, fileCursors.get(patchId) + 1);
  }

  private void removeFileCursor(PatchId patchId) {
    fileCursors.remove(patchId);
  }
}
