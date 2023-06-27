package pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;

@Getter
@AllArgsConstructor
public class RouteFileEntry {

  private final static String SEMICOLON = ";";
  private final static String COMMA = ",";

  private final long step;
  private final double carLength;
  private final double maxSpeed;
  private final double speed;
  private final RouteWithLocation route;

  public String toFileLine() {
    return concatenate(
      String.valueOf(step),
      String.valueOf(carLength),
      String.valueOf(maxSpeed),
      String.valueOf(speed),
      routeToString()
    );
  }

  private String routeToString() {
    return route.getRouteElements().stream()
      .map(element -> element.getJunctionId().getValue() + COMMA + element.getOutgoingLaneId().getValue() + COMMA)
      .reduce("", String::concat);
  }


  private String concatenate(String... str) {
    return String.join(SEMICOLON, str);
  }

}
