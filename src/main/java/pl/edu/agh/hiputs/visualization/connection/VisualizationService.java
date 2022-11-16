package pl.edu.agh.hiputs.visualization.connection;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.visualization.connection.producer.CarsProducer;
import pl.edu.agh.hiputs.visualization.connection.producer.SimulationNewNodesProducer;
import pl.edu.agh.hiputs.visualization.connection.producer.SimulationStateChangeProducer;
import proto.model.RUNNING_STATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisualizationService {

  private final CarsProducer carsProducer;
  private final SimulationNewNodesProducer simulationNewNodesProducer;
  private final SimulationStateChangeProducer simulationStateChangeProducer;

  public void sendCarsFromMapFragment(MapFragment mapFragment, int iterationNumber) {
    this.carsProducer.sendCars(mapFragment, iterationNumber);
  }

  public void sendSimulationStateChangeMessage(RUNNING_STATE runningState) {
    simulationStateChangeProducer.sendStateChangeMessage(runningState);
  }

  public void sendNewNodes(List<Patch> patches) {
    simulationNewNodesProducer.sendSimulationNotOsmNodesTransferMessage(patches);
  }
}
