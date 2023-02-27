package pl.edu.agh.hiputs.visualization.graphstream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;

@Disabled("It just shows GUI")
class TrivialGraphBasedVisualizerTest {

  @Test
  void showGui() {

    MapFragment mp = ExampleMapFragmentProvider.getSimpleMap2();
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