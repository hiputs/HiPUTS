package pl.edu.agh.hiputs.visualization.connection;

import static proto.model.RUNNING_STATE.RESUMED;
import static proto.model.RUNNING_STATE.STARTED;
import static proto.model.RUNNING_STATE.STOPPED;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.service.server.MessageSenderServerService;
import pl.edu.agh.hiputs.visualization.communication.messages.ResumeSimulationMessage;
import pl.edu.agh.hiputs.visualization.communication.messages.StopSimulationMessage;
import pl.edu.agh.hiputs.visualization.connection.producer.SimulationStateChangeProducer;
import proto.model.RUNNING_STATE;

@Service
@RequiredArgsConstructor
public class VisualizationSynchronisationService {

  private final SimulationStateChangeProducer simulationStateChangeProducer;
  private final MessageSenderServerService messageSenderServerService;
  private RUNNING_STATE currentSimulationState;
  private RUNNING_STATE currentVisualizationState;

  public void changeSimulationState(RUNNING_STATE running_state) {
    this.currentSimulationState = running_state;
    sendCurrentSimulationStateMessage();
  }

  private void sendCurrentSimulationStateMessage() {
    simulationStateChangeProducer.sendStateChangeMessage(currentSimulationState);
  }

  @SneakyThrows
  public synchronized void waitForVisualizationStateChangeMessage(RUNNING_STATE running_state) {
    while (!running_state.equals(currentVisualizationState)) {
      wait();
    }
  }

  public synchronized void updateVisualizationState(RUNNING_STATE running_state) {
    if (STARTED.equals(running_state) && STARTED.equals(currentSimulationState)) {
      sendCurrentSimulationStateMessage();
    }
    if (STOPPED.equals(running_state)) {
      messageSenderServerService.broadcast(new StopSimulationMessage());
    }
    if (RESUMED.equals(running_state)) {
      messageSenderServerService.broadcast(new ResumeSimulationMessage());
    }
    this.currentVisualizationState = running_state;
    notify();
  }
}
