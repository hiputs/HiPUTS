package pl.edu.agh.hiputs.visualization.graphstream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Disabled("It just shows GUI")
class TrivialGraphBasedVisualizerTest {

  @Mock
  private MapRepository mapRepository;

  @Test
  void showGui() {

    MapFragment mp = ExampleMapFragmentProvider.getSimpleMap2(mapRepository);
    TrivialGraphBasedVisualizer trivialGraphBasedVisualizer = new TrivialGraphBasedVisualizer(mp, null);

    trivialGraphBasedVisualizer.showGui();

    for (int i = 0; i < 10; i++) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
      trivialGraphBasedVisualizer.redrawCars();
    }

  }
}