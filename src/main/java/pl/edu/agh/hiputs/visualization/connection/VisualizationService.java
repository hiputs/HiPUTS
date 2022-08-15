package pl.edu.agh.hiputs.visualization.connection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.visualization.connection.producer.Producer;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisualizationService {

  private final Producer producer;

  public void sendCarsFromMapFragment(MapFragment mapFragment) {
    log.info("Start sending cars from mapFragment:" + mapFragment.getMapFragmentId());
    mapFragment.getLocalLaneIds().stream().map(mapFragment::getLaneReadable).forEach(laneReadable -> {
      CarReadable carReadable = laneReadable.getCarAtEntryReadable().orElse(null);
      while (carReadable != null) {
        this.producer.sendCar(carReadable, laneReadable, StringUtils.EMPTY);
        carReadable = laneReadable.getCarInFrontReadable(carReadable).orElse(null);
      }
    });
    log.info("End sending cars from mapFragment:" + mapFragment.getMapFragmentId());
  }

}
