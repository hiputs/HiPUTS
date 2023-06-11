package pl.edu.agh.hiputs.service.routegenerator;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.routegenerator.generator.FileInputGenerator;
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

  private final FileInputGenerator fileGenerator;
  private final Random random = new Random();
  private final MapRepository mapRepository;

  @Value("${carGenerator.filesPath}")
  private String patchesPath;

  @Override
  public void generateFiles(MapFragment fragment) {
    fragment.localPatches().forEach(patch -> generateFileForPatch(patch, fragment));
  }

  private void generateFileForPatch(Patch patch, MapFragment mapFragment) {

    var filePath = format("{0}/patch_{1}", patchesPath, patch.getPatchId().getValue());

//    TODO: przemyśl parametry -> (może generowanie per step? wywołujemy po kolei dla kazdego stepu symulacji
//     -> potrzbna ilośc stepów wiadoma)

    try (var fw = new FileWriter(filePath, false);
         var bw = new BufferedWriter(fw);
         var out = new PrintWriter(bw)) {
      new File(filePath).createNewFile();
      List<RouteWithLocation> routes = fileGenerator.generateRouteFileInput(patch, null, null, mapFragment);
      for (RouteWithLocation route : routes) {
        if (!route.getRouteElements().isEmpty()) {
//          TODO: tu np można by wstawić to generowanie ( i uzależnić w tym forze od stepu?)
          var step = 20000;
          var carLength = random.nextDouble(3.0, 5.0);
          var speed = random.nextDouble(0, 100);
          var maxSpeed = random.nextDouble(speed, speed + 20.0);
          var fileEntry = new RouteFileEntry(step, route, carLength, maxSpeed, speed);
          out.println(fileEntry.toFileLine());
        }
      }
    } catch (IOException e) {
      log.error(format("Cannot create file: {} Inner exception: {}", filePath, e.getMessage()));
    }


  }
}
