package pl.edu.agh.hiputs.model.map.roadstructure;

public enum RoadSubordination {

  /**
   * Road is subordinate
   */
  SUBORDINATED,

  /**
   * Road is not subordinate
   */
  MAIN_ROAD,

  /**
   * Road is not subject to subordination rules (e.g. outgoing road)
   */
  NONE
}
