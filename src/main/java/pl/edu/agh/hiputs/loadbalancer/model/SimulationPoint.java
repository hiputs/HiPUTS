package pl.edu.agh.hiputs.loadbalancer.model;

public enum SimulationPoint {
  FIRST_ITERATION,
  WAITING_FOR_FIRST_ITERATION,
  SECOND_ITERATION,
  SYNCHRONIZATION_AREA,
  WAITING_FOR_SECOND_ITERATION,
  LOAD_BALANCING
}
