package pl.edu.agh.hiputs.scheduler.task;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.example.ExampleCarProvider;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

@Slf4j
@RequiredArgsConstructor
public class CarRouteExtendTask implements Runnable{

  private final ExampleCarProvider carProvider;
  private final MapFragment mapFragment;
  private final LaneId laneId;
  private final int remainingTimeSteps;
  private static final int ROUTE_ELEMENTS_THRESHOLD = 15;


  @Override
  public void run() {
    try {
      LaneEditable lane = mapFragment.getLaneEditable(laneId);

      lane.streamCarsFromEntryEditable()
          .filter(car -> car.getRouteWithLocation().getRemainingRouteSize() <= ROUTE_ELEMENTS_THRESHOLD)
          .forEach(car -> {
            RoadId lastRoad = car.getRouteWithLocation().getLastRouteElement().getOutgoingRoadId();
            LaneId anyLaneId = mapFragment.getRoadEditable(lastRoad).getLanes().get(0);
            List<RouteElement> newRoute =
                carProvider.generateRouteElements(anyLaneId,
                    remainingTimeSteps / 40 + ROUTE_ELEMENTS_THRESHOLD);
            log.debug("Extending car's:"+ car.getCarId()+" route. Current route size:"+car.getRouteWithLocation().getRemainingRouteSize()+". Adding "+(newRoute.size()-1)+" elements.");

            car.extendRouteWithLocation(newRoute.subList(1, newRoute.size()));
          });
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }

  }
}
