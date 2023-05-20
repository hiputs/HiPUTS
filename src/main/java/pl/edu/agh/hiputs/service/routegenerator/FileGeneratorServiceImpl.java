package pl.edu.agh.hiputs.service.routegenerator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.routegenerator.generator.FileInputGenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static java.text.MessageFormat.format;

@Slf4j
@AllArgsConstructor
@Service
public class FileGeneratorServiceImpl implements  FileGeneratorService{

  private final FileInputGenerator fileGenerator;


  @Override
  public void generateFiles(MapFragment fragment) {
    fragment.localPatches().forEach(patch -> generateFileForPatch(patch));
  }

  private void generateFileForPatch(Patch patch) {


    String directoryPath = "src/main/resources/generator/simple_map_1";
    var filePath = format("{0}/patch_{1}", directoryPath, patch.getPatchId().getValue());

//    TODO: przemyśl parametry -> (może generowanie per step? wywołujemy po kolei dla kazdego stepu symulacji
//     -> potrzbna ilośc stepów wiadoma)
    List<RouteWithLocation> routes = fileGenerator.generateRouteFileInput(patch, null, null, null);

    try(FileWriter fw = new FileWriter(filePath, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw))
    {
      for(RouteWithLocation route: routes){
        if(!route.getRouteElements().isEmpty()) {
          String semicolon = ";";
//          TODO: tu np można by wstawić to generowanie ( i uzależnić w tym forze od stepu?)
          String step = 20000 + semicolon;

          List<RouteElement> routeElements = route.getRouteElements();
          String startJunctionId = routeElements.get(0).getJunctionId().getValue() + semicolon;
          String endLaneId = routeElements.get(routeElements.size()-1).getOutgoingLaneId().getValue() + semicolon;
//          TODO: miejsce na informacje o samochodzie... (pamietać o zmianach w RouteReaader)
          String routeString = "";
          String comma = ",";
          for(RouteElement routeElement:route.getRouteElements()){
            String routeElementString = routeElement.getJunctionId().getValue() + comma +
              routeElement.getOutgoingLaneId().getValue() + comma;
            routeString += routeElementString;
          }
          String newLine = step + startJunctionId + endLaneId + routeString.substring(0, routeString.length() - 1);
          out.println(newLine);
        }
      }
    } catch (IOException e) {
      log.error(String.valueOf(e));
    }


  }
}
