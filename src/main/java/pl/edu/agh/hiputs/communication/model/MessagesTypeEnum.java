package pl.edu.agh.hiputs.communication.model;

import java.util.Arrays;
import java.util.List;
import pl.edu.agh.hiputs.communication.model.messages.LoadInfoMessage;
import pl.edu.agh.hiputs.communication.model.messages.MapReadyToReadMessage;
import pl.edu.agh.hiputs.communication.model.messages.NeighbourConnectionMessage;
import pl.edu.agh.hiputs.communication.model.messages.SerializedPatchTransfer;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedCar;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedCrossroadDecisionProperties;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedDecision;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedLane;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedRouteElement;
import pl.edu.agh.hiputs.communication.model.serializable.WorkerDataDto;
import pl.edu.agh.hiputs.statistics.SimulationPoint;
import pl.edu.agh.hiputs.statistics.worker.IterationStatisticsServiceImpl.IterationInfo;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.DecisionStatistic;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.LoadBalancingCostStatistic;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.LoadBalancingStatistic;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.MapStatistic;

import pl.edu.agh.hiputs.visualization.communication.messages.StopSimulationMessage;

public enum MessagesTypeEnum {

  // server - worker messages
  /**
   * Information from worker, when it started and actually it is ready for start work
   */
  WorkerConnectionMessage,
  /**
   * Message with map fragment and neighbouring with connection parameter
   */
  ServerInitializationMessage,
  /**
   * Information from server when CSV map file is ready to read for worker
   */
  MapReadyToRead,
  /**
   * Information from worker when map is parsed and worker is ready for start simulation
   */
  CompletedInitializationMessage,
  /**
   * Command from server to worker, after this message worker should start calculation
   */
  RunSimulationMessage,
  /**
   * Information from worker about finish simulation
   */
  FinishSimulationMessage,
  /**
   * If statistic is enabled, after finish simulation worker send statistic info
   */
  FinishSimulationStatisticMessage,
  /**
   * Information about disconnect worker
   */
  WorkerDisconnectMessage,

  //worker - worker messages
  /**
   * Transfer incoming cars set after decision stage
   */
  CarTransferMessage,
  /**
   * Synchronize state of cars on border patches between workers
   */
  BorderSynchronizationMessage,
  /**
   * Send Patch to neighbour
   */
  PatchTransferMessage,
  /**
   * Notify adjacent areas to upload the patch
   */
  PatchTransferNotificationMessage,

  /**
   * Pack of notifications informing adjacent areas to upload the patch
   */
  GroupOfPatchTransferNotificationMessage,

  /**
   * Info adjacent areas about load
   */
  LoadInfo,

  /**
   * Synchronization message
   */
  LoadSynchronizationMessage,

  /**
   * Free ticket list message
   */
  AvailableTicketMessage,

  /**
   * select ticket message
   */
  SelectTicketMessage,

  /**
   * Info from service to shut down after simulation
   */
  ShutDownMessage,

  /**
   * Stop simulation
   */
  StopSimulationMessage,

  /**
   * Resume simulation
   */
  ResumeSimulationMessage,

  /**
   * Visualization State change message
   */
  VisualizationStateChangeMessage;

  public static List<MessagesTypeEnum> getWorkerMessages() {
    return Arrays.asList(CarTransferMessage, BorderSynchronizationMessage, PatchTransferMessage,
        PatchTransferNotificationMessage, GroupOfPatchTransferNotificationMessage, LoadInfo, AvailableTicketMessage,
        SelectTicketMessage);
  }

  public static List<Class> getMessagesClasses() {
    return Arrays.asList(pl.edu.agh.hiputs.communication.model.messages.CarTransferMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.BorderSynchronizationMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.PatchTransferMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.PatchTransferNotificationMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.GroupOfPatchTransferNotificationMessage.class,
        LoadInfoMessage.class, pl.edu.agh.hiputs.communication.model.messages.AvailableTicketMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.SelectTicketMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.ShutDownMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.WorkerConnectionMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.CompletedInitializationMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage.class, MapReadyToReadMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.RunSimulationMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.FinishSimulationMessage.class,
        pl.edu.agh.hiputs.communication.model.messages.FinishSimulationStatisticMessage.class,
        NeighbourConnectionMessage.class, WorkerDataDto.class, ConnectionDto.class, NeighbourConnectionDto.class,
        SerializedCar.class, SerializedRouteElement.class, SerializedLane.class, SerializedPatchTransfer.class,
        LoadBalancingStatistic.class, DecisionStatistic.class, LoadBalancingCostStatistic.class, IterationInfo.class,
        MapStatistic.class, SimulationPoint.class, SerializedDecision.class,
        SerializedCrossroadDecisionProperties.class);
  }
}
