package pl.edu.agh.hiputs.statistics;

public enum SimulationPoint {

  FIRST_ITERATION,
  WAITING_RECEIVING_CARS,
  SECOND_ITERATION_UPDATING_CARS,
  SECOND_ITERATION_NOTIFY,
  SYNCHRONIZATION_AREA_SEND_PATCHES,
  WAITING_RECEIVING_PATCHES,
  LOAD_BALANCING,

  DECISION_STAGE,
  SENDING_CARS,
  LOAD_BALANCING_START,
  LOAD_BALANCING_NOTIFICATIONS,
  MANAGE_CARS,
  FULL_STEP,

  WORKER_INITIALIZATION,
  WORKER_MAP_BUILD,
  WORKER_INITIAL_CAR_GENERATION,
  WORKER_SIMULATION,

  SERVER_INITIALIZATION,
  SERVER_INIT_MAP,
  SERVER_WORKER_CONNECTION,
  SERVER_MAP_PARTITION,
  SERVER_SIMULATION,
  SERVER_APP

}
