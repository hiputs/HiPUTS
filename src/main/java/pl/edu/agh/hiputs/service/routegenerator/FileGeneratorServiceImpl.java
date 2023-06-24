package pl.edu.agh.hiputs.service.routegenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.alg.util.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.configuration.Configuration;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.routegenerator.generator.FileInputGenerator;
import pl.edu.agh.hiputs.service.routegenerator.generator.TimeBasedCarGeneratorConfig;
import pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator.RouteFileEntry;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.io.*;
import java.util.List;
import java.util.Random;

import static java.text.MessageFormat.format;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileGeneratorServiceImpl implements FileGeneratorService {

  private final Configuration configuration;
  private final TimeBasedCarGeneratorConfig timeBasedCarGeneratorConfig;
  private final FileInputGenerator fileGenerator;
  private final Random random = new Random();
  private final MapRepository mapRepository;

  @Value("${carGenerator.filesPath}")
  private String patchesPath;

  @Override
  public void generateFiles(MapFragment fragment) {
    var startStep = timeBasedCarGeneratorConfig.getConfigEntries().stream().findFirst().get().getStartStep();
    var endStep = timeBasedCarGeneratorConfig.getConfigEntries().get(timeBasedCarGeneratorConfig.getConfigEntries().size() - 1).getEndStep();
    fragment.localPatches().forEach(patch -> generateFileForPatch(patch, fragment, startStep, endStep));
  }

  private void generateFileForPatch(Patch patch, MapFragment mapFragment, int startStep, int endstep) {

    var filePath = format("{0}/patch_{1}", patchesPath, patch.getPatchId().getValue());

    try (var fw = new FileWriter(filePath, false);
         var bw = new BufferedWriter(fw);
         var out = new PrintWriter(bw)) {
      new File(filePath).createNewFile();
      List<Pair<RouteWithLocation, Integer>> routesWithStep = fileGenerator.generateRouteFileInput(patch, startStep, endstep, mapFragment, mapRepository, configuration.isTestMode());
      for (Pair<RouteWithLocation, Integer> pair : routesWithStep) {
        RouteWithLocation route = pair.getFirst();
        int step = pair.getSecond();
        if (!route.getRouteElements().isEmpty()) {
          var carLength = random.nextDouble(configuration.getCarMinLengthInMeters(), configuration.getCarMaxLengthInMeters());
          var speed = random.nextDouble(0, configuration.getCarUpSpeedBoundaryInMetersPerSecond());
          var maxSpeed = random.nextDouble(speed, configuration.getCarUpMaxSpeedBoundaryInMetersPerSecond());
          var fileEntry = new RouteFileEntry(step, route, carLength, maxSpeed, speed);
          out.println(fileEntry.toFileLine());
        }
      }
    } catch (IOException e) {
      log.error(format("Cannot create file: {} Inner exception: {}", filePath, e.getMessage()));
    }


  }
}
