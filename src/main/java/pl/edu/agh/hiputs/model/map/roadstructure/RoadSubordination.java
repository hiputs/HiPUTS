package pl.edu.agh.hiputs.model.map.roadstructure;

public enum RoadSubordination {

  /**
   * Road is subordinate
   */
  SUBORDINATE,

  /**
   * Road is not subordinate
   */
  NOT_SUBORDINATE,

  /**
   * Road is not subject to subordination rules (e.g. outgoing road)
   */
  NONE
}
