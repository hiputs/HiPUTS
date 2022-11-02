package pl.edu.agh.hiputs.communication.model;

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
   *  Transfer incoming cars set after decision stage
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
   * Info adjacent areas about load
   */
  LoadInfo,

  /**
   * Synchronization message
   */
  LoadSynchronizationMessage,

  /**
   * Info from service to shut down after simulation
   */
  ShutDownMessage
}
