package pl.edu.agh.hiputs.model.map.roadstructure;

public enum LaneSubordination {

  /**
   * Lane is subordinate
   */
  SUBORDINATE,

  /**
   * Lane is not subordinate
   */
  NOT_SUBORDINATE,

  /**
   * Lane is not subject to subordination rules (e.g. outgoing lane)
   */
  NONE
}
