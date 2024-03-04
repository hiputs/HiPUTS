package pl.edu.agh.hiputs.visualization.connection;

import static proto.model.RUNNING_STATE.CLOSED;
import static proto.model.RUNNING_STATE.RESUMED;
import static proto.model.RUNNING_STATE.STARTED;
import static proto.model.RUNNING_STATE.STOPPED;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.ShutDownMessage;
import pl.edu.agh.hiputs.communication.service.server.MessageSenderServerService;
import pl.edu.agh.hiputs.visualization.communication.messages.ResumeSimulationMessage;
import pl.edu.agh.hiputs.visualization.communication.messages.StopSimulationMessage;
import pl.edu.agh.hiputs.visualization.connection.producer.SimulationStateChangeProducer;
import proto.model.RUNNING_STATE;
import proto.model.VisualizationStateChangeMessage;

@Slf4j
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
  public synchronized void waitForVisualizationStateChangeMessage(RUNNING_STATE visualizationStateChange) {
    log.info("Application is waiting for visualization state change: {}", visualizationStateChange);
    while (!visualizationStateChange.equals(currentVisualizationState)) {
      wait();
    }
  }

  public synchronized void applyVisualizationChange(VisualizationStateChangeMessage visualizationStateChangeMessage) {
    this.currentVisualizationState = visualizationStateChangeMessage.getStateChange();
    messageSenderServerService.broadcast(
        new pl.edu.agh.hiputs.visualization.communication.messages.VisualizationStateChangeMessage(
            visualizationStateChangeMessage));
    switch (currentVisualizationState) {
      case STARTED -> {
        if (currentSimulationState != null) {
          sendCurrentSimulationStateMessage();
        }
      }
      case STOPPED -> {
        if (STARTED.equals(currentSimulationState) || RESUMED.equals(currentSimulationState)) {
          messageSenderServerService.broadcast(new StopSimulationMessage());
          changeSimulationState(STOPPED);
        } else if (STOPPED.equals(currentSimulationState)) {
          sendCurrentSimulationStateMessage();
        }
      }
      case RESUMED -> {
        if (STOPPED.equals(currentSimulationState)) {
          messageSenderServerService.broadcast(new ResumeSimulationMessage());
          changeSimulationState(RESUMED);
        }
      }
      case CLOSED -> {
        if (STARTED.equals(currentSimulationState) || STOPPED.equals(currentSimulationState) || RESUMED.equals(currentSimulationState)) {
          messageSenderServerService.broadcast(new ShutDownMessage());
          changeSimulationState(CLOSED);
        }
      }
    }
    notify();
  }
}