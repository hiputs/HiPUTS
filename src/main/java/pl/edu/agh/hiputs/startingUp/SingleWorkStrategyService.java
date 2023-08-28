package pl.edu.agh.hiputs.startingUp;

import static java.lang.Thread.sleep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.loadbalancer.LocalLoadMonitorService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.simulation.MapFragmentExecutor;
import pl.edu.agh.hiputs.visualization.graphstream.TrivialGraphBasedVisualizer;

@Slf4j
@Service
@RequiredArgsConstructor
public class SingleWorkStrategyService implements Strategy {

  private final MapFragmentExecutor mapFragmentExecutor;
  private final LocalLoadMonitorService localLoadMonitorService;
  private final MapRepository mapRepository;

  @Override
  public void executeStrategy() throws InterruptedException {
    log.info("Start work in single mode");
    mapFragmentExecutor.setMapFragment(ExampleMapFragmentProvider.getSimpleMap4(mapRepository));
    localLoadMonitorService.init(mapFragmentExecutor.getMapFragment());
    TrivialGraphBasedVisualizer graphBasedVisualizer = new TrivialGraphBasedVisualizer(mapFragmentExecutor.getMapFragment(), null);

    graphBasedVisualizer.showGui();
    sleep(1000);

    while (true) {
      mapFragmentExecutor.run(-1);
      graphBasedVisualizer.redrawCars();
      sleep(5);
    }
  }
}
